package nz.govt.natlib.dashboard.ui;

import nz.govt.natlib.dashboard.common.DashboardConstants;
import nz.govt.natlib.dashboard.domain.entity.EntityWhitelistSetting;
import nz.govt.natlib.dashboard.domain.service.WhitelistSettingService;
import nz.govt.natlib.dashboard.ui.controller.WhitelistController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = WhitelistController.class)
@AutoConfigureMockMvc(addFilters = false)
// Force the controller to be loaded as a bean
@Import(WhitelistController.class)
public class WhitelistControllerTest extends AbstractControllerTest {
    @MockitoBean
    private WhitelistSettingService whitelistSettingService;

    private List<EntityWhitelistSetting> genWhitelistData() {
        List<EntityWhitelistSetting> data = new ArrayList<>();
        EntityWhitelistSetting user1 = new EntityWhitelistSetting();
        user1.setId(1L);
        user1.setWhiteUserName("user1");
        user1.setWhiteUserRole("admin");
        data.add(user1);
        return data;
    }

    @Test
    public void testGetAll() throws Exception {
        List<EntityWhitelistSetting> data = this.genWhitelistData();
        given(whitelistSettingService.getAllWhitelistSettings()).willReturn(data);

        mockMvc.perform(get(DashboardConstants.PATH_SETTING_WHITELIST_ALL_GET)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testGetWhitelistDetail() throws Exception {
        List<EntityWhitelistSetting> data = this.genWhitelistData();
        EntityWhitelistSetting user1 = data.get(0);
        given(whitelistSettingService.getWhitelistDetail(1L)).willReturn(user1);

        mockMvc.perform(get(DashboardConstants.PATH_SETTING_WHITELIST_DETAIL + "?id=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user1.getId()))
                .andExpect(jsonPath("$.whiteUserName").value(user1.getWhiteUserName()))
                .andExpect(jsonPath("$.whiteUserRole").value(user1.getWhiteUserRole()));
    }
}
