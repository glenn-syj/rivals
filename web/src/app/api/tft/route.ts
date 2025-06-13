import { NextResponse } from "next/server";

let tftDataCache: any = null;
let lastFetchTime: number = 0;
const CACHE_DURATION = 1000 * 60 * 60 * 24; // 24 hours
const CURRENT_SET = "14"; // 현재 TFT 세트 버전

async function fetchTftData() {
  const currentTime = Date.now();

  // Return cached data if it's still valid
  if (tftDataCache && currentTime - lastFetchTime < CACHE_DURATION) {
    return tftDataCache;
  }

  try {
    const response = await fetch(
      "https://raw.communitydragon.org/latest/cdragon/tft/ko_kr.json"
    );
    const data = await response.json();

    // Update cache
    tftDataCache = data;
    lastFetchTime = currentTime;

    return data;
  } catch (error) {
    console.error("Error fetching TFT data:", error);
    // If fetch fails and we have cached data, return it even if expired
    if (tftDataCache) return tftDataCache;
    throw error;
  }
}

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get("type"); // 'champions' or 'traits'
  const id = searchParams.get("id"); // specific item id
  const set = searchParams.get("set") || CURRENT_SET; // TFT set version

  try {
    const data = await fetchTftData();

    if (!type || !id) {
      throw new Error("Type and id parameters are required");
    }

    // Get set data
    const setData = data.sets[set];
    if (!setData) {
      throw new Error(`TFT Set ${set} not found`);
    }

    switch (type) {
      case "champions":
        const champion = setData.champions.find(
          (champ: any) => champ.apiName === id
        );
        return NextResponse.json(champion || null);

      case "traits":
        const trait = setData.traits.find((trait: any) => trait.apiName === id);
        return NextResponse.json(trait || null);

      default:
        throw new Error(`Invalid type: ${type}`);
    }
  } catch (error) {
    console.error("Error in GET:", error);
    return NextResponse.json(
      {
        error:
          error instanceof Error ? error.message : "Failed to fetch TFT data",
      },
      { status: 500 }
    );
  }
}
