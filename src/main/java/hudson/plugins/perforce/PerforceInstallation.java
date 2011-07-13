/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hudson.plugins.perforce;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import java.io.IOException;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author rpetti
 */
public class PerforceInstallation extends ToolInstallation implements NodeSpecific<PerforceInstallation>, EnvironmentSpecific<PerforceInstallation>{

    private String p4Exe;
    private String p4SysRoot;
    private String p4SysDrive;

    @DataBoundConstructor
    public PerforceInstallation(String name, String home, String p4Exe, String p4SysRoot, String p4SysDrive, List<? extends ToolProperty<?>> properties){
        super(name, home, properties);
        this.p4Exe = Util.fixEmpty(p4Exe);
        this.p4SysRoot = Util.fixEmpty(p4SysRoot);
        this.p4SysDrive = Util.fixEmpty(p4SysDrive);
    }

    public String getP4Exe() {
        return p4Exe != null ? p4Exe : "INSTALLATION/p4";
    }

    public String executableWithSubstitution(String home) {
        return getP4Exe().replace("INSTALLATION", home);
    }

    public String getP4SysDrive() {
        return p4SysDrive;
    }

    public String getP4SysRoot() {
        return p4SysRoot;
    }

    public PerforceInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new PerforceInstallation(getName(), translateFor(node, log), p4Exe, p4SysRoot, p4SysDrive, getProperties().toList());
    }

    public PerforceInstallation forEnvironment(EnvVars environment) {
        return new PerforceInstallation(getName(), environment.expand(getHome()), environment.expand(p4Exe), p4SysRoot, p4SysDrive, getProperties().toList());
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<PerforceInstallation> {

        @CopyOnWrite
        @SuppressWarnings("VO_VOLATILE_REFERENCE_TO_ARRAY")
        private volatile PerforceInstallation[] installations = new PerforceInstallation[0];

        public DescriptorImpl() {
            load();
        }

        public String getDisplayName() {
            return "Perforce";
        }

        @Override
        @SuppressWarnings(value = "EI_EXPOSE_REP")
        public PerforceInstallation[] getInstallations() {
            return installations;
        }

        @Override
        public void setInstallations(PerforceInstallation... installations) {
            this.installations = installations;
            save();
        }

    }

}
