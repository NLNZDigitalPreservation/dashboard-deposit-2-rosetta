import { ref } from "vue";
import { defineStore } from "pinia";
import { FilterMatchMode, FilterOperator } from "primevue/api";
import { type UseFetchApis, useFetch } from "@/utils/rest.api";

export const depositJobList = ref([]);
export const keywords = ref();

export const jobFilters = ref({
  global: { value: null, matchMode: FilterMatchMode.CONTAINS },
  // 'id': { value: null, matchMode: FilterMatchMode.CONTAINS },
  injectionTitle: {
    operator: FilterOperator.OR,
    constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }],
  },
  "appliedFlowSetting.materialFlowName": {
    operator: FilterOperator.OR,
    constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }],
  },
  // 'stage': { value: null, matchMode: FilterMatchMode.CONTAINS },
  // 'state': { value: null, matchMode: FilterMatchMode.CONTAINS },
  // 'sipID': { value: null, matchMode: FilterMatchMode.CONTAINS },
});

export const formatDatetimeFromEpochMilliSeconds = (epochMilliSecond: any) => {
  if (!epochMilliSecond || epochMilliSecond === 0) {
    return "NA";
  }

  var date = new Date(epochMilliSecond);
  return date.toLocaleString();
};

const formatMaterialFlowGroup = (data: any) => {
  if (
    !data ||
    !data.appliedFlowSetting ||
    !data.appliedFlowSetting.materialFlowName
  ) {
    return "Unknown";
  }

  return data.appliedFlowSetting.materialFlowName;
};

const getProgressBarClass = (data: any) => {
  if (data.stage === "FINISHED" && data.state === "CANCELED") {
    return "abnormal-progressbar";
  } else {
    return "";
  }
};

const calcProgressPercent = (data: any) => {
  const stage = data.stage;
  const state = data.state;

  let percent = 0;
  if (stage === "INGEST") {
    percent = 0;
  } else if (stage === "DEPOSIT") {
    percent = 33.33;
  } else if (stage === "FINALIZE") {
    percent = 66.77;
  } else {
    percent = 100;
  }

  var percentState = 0.0;
  if (state === "INITIALED") {
    percentState = 0.1;
  } else if (state === "RUNNING" || state === "PAUSED") {
    percentState = 0.5;
  } else {
    percentState = 1.0;
  }

  percent += percentState * 33.33;

  if (percent > 99) {
    percent = 100;
  }

  // return percent.toFixed(0);
  // return parseInt(percent.toFixed(2));
  return Math.round(percent);
};

const jobFilter = (job: any, keywords: string) => {
  if (
    job.id.toString().includes(keywords) ||
    job.materialFlowName.toLowerCase().includes(keywords) ||
    job.injectionTitle.toLowerCase().includes(keywords) ||
    job.stage.toLowerCase().includes(keywords) ||
    job.state.toLowerCase().includes(keywords) ||
    job.sipID.toLowerCase().includes(keywords) ||
    job.sipModule.toLowerCase().includes(keywords) ||
    job.sipStage.toLowerCase().includes(keywords) ||
    job.sipStatus.toLowerCase().includes(keywords) 
  ) {
    return true;
  }
};

export const useJobListDTO = defineStore("JobListDTO", () => {
  const listJobs = ref([]);
  const listJobsFiltered = ref();
  const rest: UseFetchApis = useFetch();

  const fetchAllData = () => {
    rest
      .get("/restful/deposit-jobs/jobs/active/list")
      .then((data: any) => {
        listJobs.value = data.map((e: any) => ({
          ...e,
          progress: calcProgressPercent(e),
          progressClassName: getProgressBarClass(e),
          initialTime: formatDatetimeFromEpochMilliSeconds(e.initialTime),
          latestTime: formatDatetimeFromEpochMilliSeconds(e.latestTime),
          depositStartTime: formatDatetimeFromEpochMilliSeconds(
            e.depositStartTime
          ),
          depositEndTime: formatDatetimeFromEpochMilliSeconds(e.depositEndTime),
          materialFlowName: formatMaterialFlowGroup(e),
        }));
        filter(keywords.value);
      })
      .catch((err: any) => {
        console.log(err.message);
      });
  };

  const filter = (keywords: string) => {
    let lowerKeywords = "";
    if (keywords) {
      lowerKeywords = keywords.toLowerCase();
    }

    listJobsFiltered.value = listJobs.value.filter((e: any) =>
      jobFilter(e, lowerKeywords)
    );
  };
  

  return { listJobsFiltered, fetchAllData, filter };
});
