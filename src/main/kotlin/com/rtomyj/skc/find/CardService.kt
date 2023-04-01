package com.rtomyj.skc.find

import com.rtomyj.skc.dao.BanListDao
import com.rtomyj.skc.dao.Dao
import com.rtomyj.skc.dao.ProductDao
import com.rtomyj.skc.exception.SKCException
import com.rtomyj.skc.model.Card
import com.rtomyj.skc.model.CardBanListStatus
import com.rtomyj.skc.model.Product
import com.rtomyj.skc.skcsuggestionengine.TrafficService
import com.rtomyj.skc.util.HateoasLinks
import com.rtomyj.skc.util.enumeration.BanListFormat
import com.rtomyj.skc.util.enumeration.TrafficResourceType
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

/**
 * Service that is used to access card info from DB.
 */
@Service
@OptIn(DelicateCoroutinesApi::class)
class CardService @Autowired constructor(
	@Qualifier("product-jdbc") val productDao: ProductDao,
	@Qualifier("ban-list-jdbc") val banListDao: BanListDao,
	@Qualifier("jdbc") val cardDao: Dao,
	val trafficService: TrafficService
) {

	companion object {
		private val log = LoggerFactory.getLogger(this::class.java.name)
	}


	/**
	 * @param cardId The unique identifier of the card desired. Must be an 8 digit String.
	 * @param fetchAllInfo Whether all info about a card should be fetched and returned.
	 * "All Info" includes information about the packs the card is in, the ban lists the card is in, etc.
	 * @return Card object containing the information of the card desired.
	 */
	@Throws(SKCException::class)
	fun getCardInfo(cardId: String, fetchAllInfo: Boolean, clientIP: String): Card {
		log.info("Fetching info for card w/ ID: ( {} )", cardId)

		GlobalScope.launch {
			trafficService.submitTrafficData(TrafficResourceType.CARD, cardId, clientIP)
		}

		lateinit var card: Card

		if (fetchAllInfo) {
			runBlocking {
				val foundIn = mutableListOf<Product>()
				val restrictedIn = hashMapOf<BanListFormat, MutableList<CardBanListStatus>>()

				val deferredFoundIn = GlobalScope.async {
					foundIn.addAll(productDao.getProductDetailsForCard(cardId))
				}

				val deferredCardInfo = GlobalScope.async {
					card = getCardInfo(cardId)
				}

				val deferredRestrictedIn = GlobalScope.async {
					restrictedIn[BanListFormat.TCG] =
						banListDao.getBanListDetailsForCard(cardId, BanListFormat.TCG).toMutableList()
					restrictedIn[BanListFormat.MD] =
						banListDao.getBanListDetailsForCard(cardId, BanListFormat.MD).toMutableList()
					restrictedIn[BanListFormat.DL] =
						banListDao.getBanListDetailsForCard(cardId, BanListFormat.DL).toMutableList()

					for (list in restrictedIn.values) {
						HateoasLinks.setLinks(list)
					}
				}

				deferredCardInfo.await()

				deferredRestrictedIn.await()
				card.restrictedIn = restrictedIn

				deferredFoundIn.await()
				card.foundIn = foundIn
			}
		} else {
			card = getCardInfo(cardId)
		}
		return card
	}

	fun getCardInfo(cardId: String): Card {
		val card = cardDao.getCardInfo(cardId)
		card.monsterAssociation?.transformMonsterLinkRating()

		card.setLinks()
		return card
	}
}