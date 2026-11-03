
public class HelloWorldService {

    private String message;


    public HelloWorldService() {
        this.message = "Hello World";
    }

    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMessageAvailable() {
        return message != null && !message.isEmpty();
    }
}
