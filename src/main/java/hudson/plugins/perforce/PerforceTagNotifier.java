package hudson.plugins.perforce;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Launcher;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.util.logging.Logger;

import hudson.EnvVars;
import hudson.model.Action;

/**
 * PerforceTagNotifier
 * Adds the option to automatically tag a build upon build completion.
 *
 * @author rpetti
 */
public class PerforceTagNotifier extends Notifier {
    protected static final Logger LOGGER = Logger.getLogger(PerforceTagNotifier.class.getName());

    public String rawLabelName;
    public boolean onlyOnSuccess = true;

    public boolean isOnlyOnSuccess() {
        return onlyOnSuccess;
    }

    public void setOnlyOnSuccess(boolean onlyOnSuccess) {
        this.onlyOnSuccess = onlyOnSuccess;
    }

    public String getRawLabelName() {
        return rawLabelName;
    }

    public void setRawLabelName(String rawLabelName) {
        this.rawLabelName = rawLabelName;
    }
    
    @DataBoundConstructor
    public PerforceTagNotifier(String rawLabelName, boolean onlyOnSuccess){
        this.rawLabelName = rawLabelName;
        this.onlyOnSuccess = onlyOnSuccess;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        if(!onlyOnSuccess || build.getResult() == Result.SUCCESS){

            listener.getLogger().println("Labelling Build in Perforce using " + rawLabelName);

            String labelName = PerforceSCM.substituteParameters(rawLabelName, build);

            if(labelName == null || labelName.equals("")){
                listener.getLogger().println("Label Name is empty, cannot label.");
                return false;
            }
            PerforceTagAction tagAction = (PerforceTagAction) build.getAction(PerforceTagAction.class);

            try {
                if(tagAction == null){
                    listener.getLogger().println("Not found Perforce SCM checkout in Promotion build, trying to find it in build job");
                    EnvVars env = build.getEnvironment(listener);

                    AbstractProject<?,?> targetBuild = Hudson.getInstance().getItemByFullName(env.get("PROMOTED_JOB_NAME"), AbstractProject.class);
                    tagAction = (PerforceTagAction) targetBuild.getBuildByNumber(Integer.parseInt(env.get("PROMOTED_NUMBER"))).getAction(PerforceTagAction.class);

                    if(tagAction == null){
                        listener.getLogger().println("Could not label build in perforce; is it a valid perforce job?");
                        return false;
                    }
                }

                tagAction.tagBuild(labelName, "Label automatically generated by Hudson Perforce Plugin.");
            } catch (IOException e) {
                listener.getLogger().println(e.getMessage());
                return false;
            }

            listener.getLogger().println("Label '" + labelName + "' successfully generated.");
        }

        return true;
    }

    public static DescriptorImpl descriptor() {
        return Hudson.getInstance().getDescriptorByType(PerforceTagNotifier.DescriptorImpl.class);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Create or Update Label in Perforce";
        }
        
    }

}
