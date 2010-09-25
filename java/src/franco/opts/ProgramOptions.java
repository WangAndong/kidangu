package franco.opts;

import april.config.*;
import april.util.*;

public class ProgramOptions
{
    static GetOpt gopt;
    static Config config;


    public static GetOpt getOptions()
    {
        return gopt;
    }

    public static void setOptions(GetOpt g)
    {
        if (gopt != null) {
            throw new IllegalStateException("Program options can be initialized only once");
        }

        gopt = g;
    }

    public static Config getConfig()
    {
        return config;
    }

    public static void setConfig(Config c)
    {
        if (config != null) {
            throw new IllegalStateException("Program config can be initialized only once");
        }

        config = c;
    }
}
