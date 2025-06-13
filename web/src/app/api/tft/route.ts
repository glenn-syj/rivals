import { NextResponse } from "next/server";
import { promises as fs } from "fs";
import path from "path";

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
    // Read the local JSON file from public directory
    const filePath = path.join(
      process.cwd(),
      "public",
      "data",
      "sets-modified.json"
    );
    const fileContent = await fs.readFile(filePath, "utf-8");
    const data = JSON.parse(fileContent);

    // Update cache
    tftDataCache = data;
    lastFetchTime = currentTime;

    return tftDataCache;
  } catch (error) {
    console.error("Error reading TFT data:", error);
    // If reading fails and we have cached data, return it even if expired
    if (tftDataCache) return tftDataCache;
    throw error;
  }
}

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get("type"); // 'champions', 'traits', or 'items'
  const id = searchParams.get("id"); // specific item id
  const set = searchParams.get("set") || CURRENT_SET; // TFT set version

  try {
    const data = await fetchTftData();

    if (!type || !id) {
      throw new Error("Type and id parameters are required");
    }

    // Handle items separately as they're not set-specific
    if (type === "items") {
      const item = data.items?.find((item: any) => item.apiName === id);
      return NextResponse.json(item || null);
    }

    // Get set data for champions and traits
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
