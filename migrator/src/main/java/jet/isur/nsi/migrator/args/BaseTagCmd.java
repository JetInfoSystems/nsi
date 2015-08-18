package jet.isur.nsi.migrator.args;

import com.beust.jcommander.Parameter;

public class BaseTagCmd {

    @Parameter(names = "-tag", description="", required = true)
    private String tag;

    public String getTag() {
        return tag;
    }

}
