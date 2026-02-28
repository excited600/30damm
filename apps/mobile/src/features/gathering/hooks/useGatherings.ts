import { useQuery } from "@tanstack/react-query";
import apiClient from "@/api/client";
import type { Gathering, GatheringFilters } from "@/api/types/gathering";

async function getGatherings(filters: GatheringFilters): Promise<Gathering[]> {
  const { data } = await apiClient.get<Gathering[]>("/api/gatherings", { params: filters });
  return data;
}

export function useGatherings(filters: GatheringFilters = {}) {
  return useQuery({
    queryKey: ["gatherings", filters],
    queryFn: () => getGatherings(filters),
  });
}
