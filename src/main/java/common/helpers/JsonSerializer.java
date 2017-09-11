package common.helpers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonSerializer {
    private static Logger logger = LogManager.getLogger(JsonSerializer.class.getSimpleName());

    public static String Serialize(Object obj) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(obj);
            return json;
        }
        catch (JsonProcessingException ex) {
            logger.error(ex);
            return "";
        }

    }
    public static <T> T Deserialize(String json, Class<T> type) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            T obj = mapper.readValue(json, type);
            return obj;
        }
        catch (IOException ex) {
            logger.error(ex);
            return null;
        }

    }
}
