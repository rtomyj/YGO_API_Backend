package com.rtomyj.yugiohAPI.controller;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.rtomyj.yugiohAPI.helper.LogHelper;
import com.rtomyj.yugiohAPI.model.Card;
import com.rtomyj.yugiohAPI.service.CardService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Configures endpoint(s) that can be used to get card data for cards stored in database.
 */
@RequestMapping(path="${ygo.endpoints.card-v1}", produces = "application/json; charset=UTF-8")
@RestController
@CrossOrigin(origins = "*")
@Api(description = "", tags = "Card")
public class CardController
{
	/**
	 * Service object used to interface with DB DAO.
	 */
	@Autowired
	private CardService cardService;

	/**
	 * Object with information about http request.
	 */
	@Autowired
	private HttpServletRequest httpRequest;

	/**
	 * Base url for this endpoint.
	 */
	@Autowired
	@Value("${ygo.endpoints.card-v1}")
	private String endPoint;

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Cache used to store card data to prevent querying DB.
	 */
	@Autowired
	@Qualifier("cardsCache")
	private Map<String, Card> CARD_CACHE;



	/**
	 * Accepts a cardID which is used to query the DB/Cache to get information about the card.
	 * cardID must be in proper format. A regular expression is used to validate the format. If the format isn't correct, card cannot be looked up.
	 * If Card cannot be looked up, an appropriate HTTP response is sent.
	 *
	 * If the cardID is in proper format, the DB/Cache will be queried. If cardID is found in DB/Cache, a card object will be returned
	 * , else only an appropriate HTTP response is sent.
	 * @param cardID The unique identification of the card desired.
	 * @return Card object as a response.
	 */
	@GetMapping("{cardID}")
	@ResponseBody
	@ApiOperation(value = "Get information about a specific card", response = ResponseEntity.class, tags = "Card")
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 204, message = "Request yielded no content"),
		@ApiResponse(code = 400, message = "Malformed request, make sure cardID is valid")
	})
	public ResponseEntity<Card> getCard(@PathVariable("cardID") String cardID)
	{
		String requestIP = httpRequest.getRemoteHost();	// IP address of the client accessing endpoint

		 /*
			Checks user provided cardID with regex.
			If cardID fails, HTTP status code with no content is sent.
		 */
		Pattern cardIDPattern = Pattern.compile("[0-9]{8}");
		if (!cardIDPattern.matcher(cardID).matches())
		{
			HttpStatus status = HttpStatus.BAD_REQUEST;
			LOG.info(LogHelper.requestStatusLogString(requestIP, cardID, endPoint, status));
			return new ResponseEntity<>(status);
		}


		/*
			Checks the cache.
			If requested card is in cache, return it.
		*/
		Card cachedCard = CARD_CACHE.get(cardID);
		if (cachedCard != null)
		{
			HttpStatus status = HttpStatus.OK;
			LOG.info(LogHelper.requestStatusLogString(requestIP, cardID, endPoint, status, true));
			return new ResponseEntity<>(cachedCard, status);
		}
		/*
			If requested card isn't in cache, attempt to retrieve from DB.
			If DB returns a result, save result in cache and return the Card object.
		*/
		else
		{
			Card foundCard = cardService.getCardInfo(cardID);
			if (foundCard == null)
			{
				HttpStatus status = HttpStatus.NO_CONTENT;
				LOG.info(LogHelper.requestStatusLogString(requestIP, cardID, endPoint, status));
				return new ResponseEntity<>(status);
			}

			CARD_CACHE.put(cardID, foundCard);	// puts card into cache

			HttpStatus status = HttpStatus.OK;
			LOG.info(LogHelper.requestStatusLogString(requestIP, cardID, endPoint, status, false));
			return new ResponseEntity<>(foundCard, status);
		}
	}
}