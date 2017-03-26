package hotel;

public final class HotelConstants {

	public HotelConstants() {}

    // Slots name
    public static final String SLOT_FOOD            = "food";
    public static final String SLOT_NUMBER          = "number";
    public static final String SLOT_TIME            = "time";
    public static final String SLOT_SOMETHING       = "something";
    
    // Sesssion attributes
    public static final String ATT_INTERVAL         = "att_interval";
    public static final String ATT_FOOD_ORDER       = "att_food_order";
    public static final String ATT_USER_ID          = "att_user_id";

    // response string for various cases
    public static final String GOODBYE_RESPONSE     = "Goodbye";
    public static final String WELCOME_RESPONSE     = 
        "Welcome to Hotel. I can help you with various services like " +
        "order food, room service and many more. To know more, try help.";
    public static final String WELCOME_REPROMPT     = 
        "Welcome to Hotel. I can help you with various services like " +
        "order food, room service and many more. To know more, try help. ";
    public static final String REQUEST_REPROMPT     = 
        "To know more about various services, try help";
    public static final String PARSE_ERROR          = 
        "I am sorry, I did not understood your query. Please try help";
    public static final String INVALID_FOOD      =
        "The food item name is invalid";    
    public static final String ALREADY_DONE_COUNTRY = 
        "The country has already been spoken.";
    public static final String HELP_RESPONSE        =  
        "Welcome to the Hotel. You can order food by saying I am hungry. "+
        "Set a wake up call by asking Alexa to Wake me up at time. " ;
    public static final String TRY_AGAIN            = 
        "It is taking a lot of time. Do you want me to try again?";
    public static final String ORDER_PLACED         =
        "Your order has been placed. You will soon receive a message confirmation";

}