package configurations;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

import constants.ConfigurationConstants;

public class CloudinaryConfig {
    public static Cloudinary getCloudinary(){
        Map config = new HashMap();
        config.put("cloud_name", ConfigurationConstants.CLOUDINARY_NAME);
        config.put("api_key", ConfigurationConstants.CLOUDINARY_API_KEY);
        config.put("api_secret", ConfigurationConstants.CLOUDINARY_API_SECRET);
        return new Cloudinary(config);
    }
}
