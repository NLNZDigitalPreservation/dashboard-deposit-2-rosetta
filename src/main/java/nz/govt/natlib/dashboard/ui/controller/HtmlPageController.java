package nz.govt.natlib.dashboard.ui.controller;

import com.exlibris.dps.sdk.pds.PdsUserInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import nz.govt.natlib.dashboard.app.MainSecurityConfig;
import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.common.core.RosettaApi;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobStage;
import nz.govt.natlib.dashboard.common.metadata.EnumDepositJobState;
import nz.govt.natlib.dashboard.domain.repo.RepoFlowSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
public class HtmlPageController {
    private static final Logger log = LoggerFactory.getLogger(HtmlPageController.class);
    @Autowired
    private FreeMarkerConfigurer confFactory;
    @Autowired
    private MainSecurityConfig securityConfig;
    @Autowired
    private RepoFlowSetting repoFlowSetting;
    @Autowired
    private WhitelistSettingService whitelistService;
    @Autowired
    private RosettaApi rosettaApi;

    @RequestMapping(path = {"/", "/home.html", DashboardConstants.PATH_USER_INDEX_HTML}, method = {RequestMethod.GET, RequestMethod.POST})
    public void getHomeHtml(@ModelAttribute("model") ModelMap model, HttpServletRequest req, HttpServletResponse rsp) throws IOException, TemplateException {
        if (!securityConfig.isValidSession(req, rsp)) {
            rsp.sendRedirect(req.getContextPath() + DashboardConstants.PATH_USER_LOGIN_HTML);
            return;
        }

        Configuration conf = confFactory.getConfiguration();

        Template templateDepositJobs = conf.getTemplate("deposit-jobs.html");
        StringWriter swDepositJobsActive = new StringWriter();
        model.addAttribute("flowSettings", repoFlowSetting.getAll());
        model.addAttribute("listStages", Arrays.stream(EnumDepositJobStage.values()).map(EnumDepositJobStage::name).collect(Collectors.toList()));
        model.addAttribute("listStates", Arrays.stream(EnumDepositJobState.values()).map(EnumDepositJobState::name).collect(Collectors.toList()));
        templateDepositJobs.process(model, swDepositJobsActive);
        String htmlDepositJobsActive = swDepositJobsActive.toString(); //conf.getTemplate("deposit-jobs-active.html").toString();
        swDepositJobsActive.close();

//        String htmlSettingFlows = conf.getTemplate("setting-flows.html").toString();
//        String htmlSettingGlobal = conf.getTemplate("setting-global.html").toString();

        model.addAttribute("templateDepositJobs", htmlDepositJobsActive);
//        model.addAttribute("templateSettingFlows", htmlSettingFlows);
//        model.addAttribute("templateSettingGlobal", htmlSettingGlobal);

        PdsUserInfo userInfo = (PdsUserInfo) req.getSession().getAttribute(DashboardConstants.KEY_USER_INFO);
        String userName = DashboardHelper.isNull(userInfo) ? "unknown" : userInfo.getUserName();
        model.addAttribute("map_path_constants", DashboardConstants.MAP_ALL_CONSTANTS);
        model.addAttribute("userName", userName);
        model.addAttribute("redirectUrl", DashboardConstants.PATH_USER_LOGIN_HTML);

        boolean isInitialed = !whitelistService.isEmptyWhiteList();
        model.addAttribute("initialed", Boolean.toString(isInitialed));
        model.addAttribute("PATH_CONTEXT", req.getContextPath());

        printHtml("home.html", model, rsp);
        model.clear();
    }

    @RequestMapping(path = {DashboardConstants.PATH_USER_LOGIN_HTML}, method = {RequestMethod.GET, RequestMethod.POST})
    public void getLoginHtml(@ModelAttribute("model") ModelMap model, HttpServletRequest req, HttpServletResponse rsp) throws IOException, TemplateException {
        if (securityConfig.isValidSession(req, rsp)) {
            rsp.sendRedirect(req.getContextPath() + DashboardConstants.PATH_USER_INDEX_HTML);
            return;
        }

        boolean isInitialed = !whitelistService.isEmptyWhiteList();
        model.addAttribute("initialed", Boolean.toString(isInitialed));
        model.addAttribute("PATH_CONTEXT", req.getContextPath());

        printHtml("login.html", model, rsp);
    }

    private void printHtml(String templatePath, ModelMap model, HttpServletResponse rsp) {
        rsp.setContentType("text/html");

        Configuration conf = confFactory.getConfiguration();
        try {
            Template indexTemplate = conf.getTemplate(templatePath);
            indexTemplate.process(model, rsp.getWriter());
        } catch (TemplateException | IOException e) {
            log.error("Failed to response", e);
            try {
                rsp.getWriter().write(ExceptionUtils.getStackTrace(e));
            } catch (IOException ioException) {
                log.error("Failed to response", ioException);
            }
        }
    }

    private void printErrorHtml(Exception e, ModelMap model, HttpServletResponse rsp) {
        printErrorHtml(e.getMessage(), ExceptionUtils.getStackTrace(e), model, rsp);
    }

    private void printErrorHtml(String errorTitle, String errorContent, ModelMap model, HttpServletResponse rsp) {
        rsp.setContentType("text/html");

        Configuration conf = confFactory.getConfiguration();
        try {
            model.addAttribute("errorTitle", errorTitle);
            model.addAttribute("errorContent", errorContent);

            Template indexTemplate = conf.getTemplate("error.html");
            indexTemplate.process(model, rsp.getWriter());
        } catch (TemplateException | IOException e) {
            log.error("Failed to response", e);
            try {
                rsp.getWriter().write(ExceptionUtils.getStackTrace(e));
            } catch (IOException ioException) {
                log.error("Failed to response", ioException);
            }
        }
    }
}
