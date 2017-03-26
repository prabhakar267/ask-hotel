package hotel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.io.DataOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.util.Arrays;  
import java.util.List;  
import java.util.ArrayList;  
import java.util.Random;
import java.net.*;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

public class HotelSpeechlet implements Speechlet {
    
    private static final Logger log = LoggerFactory.getLogger(HotelSpeechlet.class);
    private static List<String> VALID_FOOD;
    private String endpoint = "http://mayankbadola.me:5000/";

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
        throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
        throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
        throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent       = request.getIntent();
        String intentName   = intent.getName();
        String userId       = session.getUser().getUserId();

        populateValidFood();

        
        if ("OrderFood".equals(intentName))
            return startNewOrderFood(intent, session, userId);
        if ("ContinueOrder".equals(intentName))
            return continueOrder(intent, session);
        if ("AMAZON.NoIntent".equals(intentName))
            return finalizeOrder(intent, session);
        
        if ("WakeUpCall".equals(intentName))
            return wakeUpCall(intent, session, userId);
        
        if ("OtherServices".equals(intentName))
            return otherServices(intent, session, userId);
        
        if ("AMAZON.StartOverIntent".equals(intentName))
            return getWelcomeResponse();
        if ("AMAZON.HelpIntent".equals(intentName))
            return getHelpResponse();
        if ("AMAZON.StopIntent".equals(intentName))
            return getByeResponse();
        if ("AMAZON.CancelIntent".equals(intentName))
            return getByeResponse();
        return getParseErrorResponse();
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
        throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    /**
    * Starts new order
    */
    private SpeechletResponse startNewOrderFood(final Intent intent, final Session session, final String userId) {
        
        // To make sure, the request doent contsin random words
        Slot                slot1       = intent.getSlot(HotelConstants.SLOT_SOMETHING);
        if(slot1 == null )
            return getParseErrorResponse();
        String              food        = slot1.getValue().toLowerCase();
        if(food == null )
            return getParseErrorResponse();
        
        // Valid ruquest
        String              output        = "Sure, What would you like to order? ";
        session.setAttribute(HotelConstants.ATT_INTERVAL, "start");
        session.setAttribute(HotelConstants.ATT_USER_ID, userId);
        return showResponse(output);
    }

    /**
    * Continues current/previous order
    */
    private SpeechletResponse continueOrder(final Intent intent, final Session session) {
        Slot                slot1       = intent.getSlot(HotelConstants.SLOT_FOOD);
        String              food        = slot1.getValue().toLowerCase();
        Slot                slot2       = intent.getSlot(HotelConstants.SLOT_NUMBER);
        String              number      = slot2.getValue();
        String   isFine    = (String) session.getAttribute(HotelConstants.ATT_INTERVAL);

        // Inavlid input or food
        if(slot1 == null || slot2 == null || food == null || number ==  null || 
            !VALID_FOOD.contains(food))
            return showResponse(HotelConstants.INVALID_FOOD);
        if(isFine == null)
            return getParseErrorResponse();

        String   order    = (String) session.getAttribute(HotelConstants.ATT_FOOD_ORDER);
        if(order == null)
            order = "";
        else
            order = order + " ";

        order = order + "," + number + "_" + food; 
        
        String  output        = " Order recorded. Anything else? ";
        // Update session attributes
        session.setAttribute(HotelConstants.ATT_FOOD_ORDER, order);
        return showResponse(output);
    }    

    /**
    * Finalizes current order
    */
    private SpeechletResponse finalizeOrder(final Intent intent, final Session session) {
        
        String   userId    = (String) session.getAttribute(HotelConstants.ATT_USER_ID);
        String   order    = (String) session.getAttribute(HotelConstants.ATT_FOOD_ORDER);
        Slot                slot       = intent.getSlot(HotelConstants.SLOT_FOOD);
        String output="";

        if(slot == null )
            return getParseErrorResponse();
        try {
            String line;
            URL url         = new URL(endpoint + "food");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "device=" + userId +  "&order=" + order;
        
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();


        } catch (IOException e) {
        }

        output = HotelConstants.ORDER_PLACED;

        session.setAttribute(HotelConstants.ATT_INTERVAL, null);
        session.setAttribute(HotelConstants.ATT_USER_ID, null);
        session.setAttribute(HotelConstants.ATT_FOOD_ORDER, null);
        return showResponse(output);
    }

    /**
    * Starts new order
    */
    private SpeechletResponse wakeUpCall(final Intent intent, final Session session, final String userId) {
        
        Slot                slot       = intent.getSlot(HotelConstants.SLOT_TIME);
        String              time        = slot.getValue();
        String output;
        /*String   order    = (String) session.getAttribute(HotelConstants.ATT_FOOD_ORDER);
        InputStreamReader   inputStream     = null;
        BufferedReader      bufferedReader  = null;
        StringBuilder       builder         = new StringBuilder();
        String   output;
        if(order == null)
            return showResponse(HotelConstants.PARSE_ERROR);

        try {
            String line;
            URL url         = new URL(endpoint + "&time=" + time + 
                                "&user_id=" + userId);
            inputStream     = new InputStreamReader(url.openStream(), Charset.forName("US-ASCII"));
            bufferedReader  = new BufferedReader(inputStream);
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            builder.setLength(0);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }

        if (builder.length() == 0) {
            output = HotelConstants.TRY_AGAIN;
        } else {
            output = HotelConstants.ORDER_PLACED;
            
        }*/

        output = "I will wake you at " + time;
        return showResponse(output);
    }

    /**
    * Starts new order
    */
    private SpeechletResponse otherServices(final Intent intent, final Session session, final String userId) {
        
        Slot                slot       = intent.getSlot(HotelConstants.SLOT_FOOD);
        String              service        = slot.getValue();
        String output="";

        if(slot == null || service == null)
            return getParseErrorResponse();
        try {
            String line;
            URL url         = new URL(endpoint + "services");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "device=" + userId +  "&query=" + service;
        
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();


        } catch (IOException e) {
        }


        output = output + "Your request for  " + service + " has been placed. You will be contacted soon.";
        return showResponse(output);
    }


    private SpeechletResponse getByeResponse() {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(HotelConstants.GOODBYE_RESPONSE);
        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    private SpeechletResponse getWelcomeResponse() {
        return showResponse(HotelConstants.WELCOME_RESPONSE);
    }

  
    private SpeechletResponse getParseErrorResponse() {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(HotelConstants.PARSE_ERROR);
        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    private SpeechletResponse getHelpResponse() {
        return newAskResponse(HotelConstants.HELP_RESPONSE, true,
            HotelConstants.WELCOME_REPROMPT, false);
    }

    private SpeechletResponse showResponse(String response) {
        return newAskResponse(response, false, response, false);
    }

   
    /** HELPER FUNCTIONS **/
    private void populateValidFood() {
        VALID_FOOD = new ArrayList<String>();
        VALID_FOOD.add("samosa");
        VALID_FOOD.add("sandwitch");
        VALID_FOOD.add("paneer");
        VALID_FOOD.add("pasta");
    }

   
    /**
     * Wrapper for creating the Ask response from the input strings with
     * plain text output and reprompt speeches.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        return newAskResponse(stringOutput, false, repromptText, false);
    }

    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
            String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
}