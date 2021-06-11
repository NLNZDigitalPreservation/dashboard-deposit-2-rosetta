package nz.govt.natlib.dashboard.domain.repo;

import com.google.common.io.Files;
import nz.govt.natlib.dashboard.domain.entity.EntityDepositJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

public class TestRepoDepositJob {
    protected static final File testDir = Files.createTempDir();
    private static final RepoDepositJob testInstance = new RepoDepositJobActive();
    private static final Long jobId_01 = 1L;
    private static final Long jobId_02 = 2L;
    private static final Long flowId_01 = 1L;
    private static final Long flowId_02 = 2L;

    @BeforeAll
    public static void init() throws IOException {
        ReflectionTestUtils.setField(testInstance, "systemStoragePath", testDir.getAbsolutePath());
        testInstance.init();
    }

    @Test
    public void testSave() {
        EntityDepositJob job1 = new EntityDepositJob();
        job1.setId(jobId_01);
        job1.setFlowId(flowId_01);
        testInstance.save(job1);
        job1.setFlowId(flowId_02);
        testInstance.save(job1);
        EntityDepositJob job2 = new EntityDepositJob();
        job2.setId(jobId_02);
        job2.setFlowId(flowId_01);
        testInstance.save(job2);

        assert testInstance.getByFlowId(flowId_01).size() == 1;
        assert testInstance.getByFlowId(flowId_02).size() == 1;
        assert testInstance.getAll().size() == 2;
    }

    @Test
    public void testDelete() {
        EntityDepositJob job1 = new EntityDepositJob();
        job1.setId(jobId_01);
        job1.setFlowId(flowId_01);
        testInstance.save(job1);

        testInstance.deleteById(job1.getId());

        assert testInstance.getById(job1.getId()) == null;
    }

    @Test
    public void testQuery() {
        EntityDepositJob job1 = new EntityDepositJob();
        job1.setId(jobId_01);
        job1.setFlowId(flowId_01);
        testInstance.save(job1);

        EntityDepositJob job = testInstance.getById(job1.getId());
        assert job != null;
        assert job.getId().equals(job1.getId());
    }
}
