package com.rtomyj.skc.controller.card

import com.rtomyj.skc.constant.SKCRegex
import com.rtomyj.skc.constant.SwaggerConstants
import com.rtomyj.skc.controller.YgoApiBaseController
import com.rtomyj.skc.exception.YgoException
import com.rtomyj.skc.model.card.Card
import com.rtomyj.skc.service.card.CardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * Configures endpoint(s) that can be used to get card data for cards stored in database.
 */
@RestController
@RequestMapping(path = ["/card"], produces = ["application/json; charset=UTF-8"])
@Validated
@Tag(name = SwaggerConstants.TAG_CARD_TAG_NAMED)
class CardController @Autowired constructor(
    /**
     * Service object used to interface with DB DAO.
     */
    private val cardService: CardService
) : YgoApiBaseController() {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.name)
    }


    /**
     * Accepts a cardId which is used to query the DB/Cache to get information about the card.
     * cardId must be in proper format. A regular expression is used to validate the format. If the format isn't correct, card cannot be looked up.
     * If Card cannot be looked up, an appropriate HTTP response is sent.
     *
     * If the cardId is in proper format, the DB/Cache will be queried. If cardId is found in DB/Cache, a card object will be returned
     * , else only an appropriate HTTP response is sent.
     * @param cardId The unique identification of the card desired.
     * @param fetchAllInfo Whether all info for specified card should be fetched.
     * @return Card object as a response.
     */
    @GetMapping("/{cardId}")
    @Operation(
        summary = "Get information about a specific card."
    )

    @ApiResponse(responseCode = "200", description = SwaggerConstants.HTTP_200_SWAGGER_MESSAGE)
    @ApiResponse(responseCode = "400", description = SwaggerConstants.HTTP_400_SWAGGER_MESSAGE)
    @ApiResponse(responseCode = "404", description = SwaggerConstants.HTTP_404_SWAGGER_MESSAGE)
    @ApiResponse(responseCode = "500", description = SwaggerConstants.HTTP_500_SWAGGER_MESSAGE)
    @Throws(YgoException::class)
    fun getCard(
        @Parameter(
            name = SwaggerConstants.CARD_ID_DESCRIPTION,
            example = "40044918",
            required = true,
            schema = Schema(implementation = String::class)
        )
        @NotNull
        @Pattern(
            regexp = SKCRegex.CARD_ID,
            message = "Card ID doesn't have correct format."
        )
        @PathVariable("cardId") cardId: String,
        @Parameter(
            name = SwaggerConstants.CARD_FETCH_ALL_DESCRIPTION,
            example = "true",
            required = false,
            schema = Schema(
                implementation = Boolean::class
            )
        )
        @RequestParam(value = "allInfo", defaultValue = "false") fetchAllInfo: Boolean = false
    ): ResponseEntity<Card> {
        log.info("Retrieving card info for using ID: {}.", cardId)
        val foundCard = cardService.getCardInfo(cardId, fetchAllInfo)
        log.info(
            "Successfully retrieved card info for: {}, w/ all info: {}.",
            cardId,
            fetchAllInfo
        )

        return ResponseEntity.ok(foundCard)
    }
}