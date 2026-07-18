package com.example.druiddemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "druid.monitor")
public class DruidMonitorProperties {

    private final StatViewServletProperties statViewServlet = new StatViewServletProperties();
    private final WebStatFilterProperties webStatFilter = new WebStatFilterProperties();

    public StatViewServletProperties getStatViewServlet() {
        return statViewServlet;
    }

    public WebStatFilterProperties getWebStatFilter() {
        return webStatFilter;
    }

    public static class StatViewServletProperties {
        private boolean enabled = true;
        private String urlPattern = "/druid/*";
        private String loginUsername = "admin";
        private String loginPassword = "admin123";
        private String resetEnable = "false";
        private String allow = "";
        private String deny = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public String getLoginUsername() {
            return loginUsername;
        }

        public void setLoginUsername(String loginUsername) {
            this.loginUsername = loginUsername;
        }

        public String getLoginPassword() {
            return loginPassword;
        }

        public void setLoginPassword(String loginPassword) {
            this.loginPassword = loginPassword;
        }

        public String getResetEnable() {
            return resetEnable;
        }

        public void setResetEnable(String resetEnable) {
            this.resetEnable = resetEnable;
        }

        public String getAllow() {
            return allow;
        }

        public void setAllow(String allow) {
            this.allow = allow;
        }

        public String getDeny() {
            return deny;
        }

        public void setDeny(String deny) {
            this.deny = deny;
        }
    }

    public static class WebStatFilterProperties {
        private boolean enabled = true;
        private String urlPattern = "/*";
        private String exclusions = "*.js,*.gif,*.jpg,*.jpeg,*.png,*.css,*.ico,/druid/*";
        private String sessionStatEnable = "true";
        private String sessionStatMaxCount = "1000";
        private String principalSessionName = "username";
        private String principalCookieName = "username";
        private String profileEnable = "true";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public String getExclusions() {
            return exclusions;
        }

        public void setExclusions(String exclusions) {
            this.exclusions = exclusions;
        }

        public String getSessionStatEnable() {
            return sessionStatEnable;
        }

        public void setSessionStatEnable(String sessionStatEnable) {
            this.sessionStatEnable = sessionStatEnable;
        }

        public String getSessionStatMaxCount() {
            return sessionStatMaxCount;
        }

        public void setSessionStatMaxCount(String sessionStatMaxCount) {
            this.sessionStatMaxCount = sessionStatMaxCount;
        }

        public String getPrincipalSessionName() {
            return principalSessionName;
        }

        public void setPrincipalSessionName(String principalSessionName) {
            this.principalSessionName = principalSessionName;
        }

        public String getPrincipalCookieName() {
            return principalCookieName;
        }

        public void setPrincipalCookieName(String principalCookieName) {
            this.principalCookieName = principalCookieName;
        }

        public String getProfileEnable() {
            return profileEnable;
        }

        public void setProfileEnable(String profileEnable) {
            this.profileEnable = profileEnable;
        }
    }
}
