package jet.nsi.common.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jet.nsi.api.NsiServiceException;
import jet.nsi.api.platform.NsiPlatform;

public class NsiPlatformFactory {
    private static final Logger log = LoggerFactory.getLogger(NsiPlatformFactory.class);
    
    public NsiPlatform create(String className) {
        Class<?> nsiPlatformClass;
        try {
            nsiPlatformClass = getClass().getClassLoader().loadClass(className);
            return (NsiPlatform) nsiPlatformClass.newInstance();
        } catch (Exception e) {
            log.error("create[{}] -> error",className, e);
            throw new NsiServiceException(e.getMessage());
        }
    }
}
