// TFT Set 관련 타입
export interface TftSet {
  name: string;
  number: number;
  traits: TftTrait[];
  champions: TftChampion[];
  items: TftItem[];
}

// TFT 챔피언 관련 타입
export interface TftChampion {
  id: string;
  name: string;
  tier: number;
  image: {
    full: string;
    sprite: string;
    group: string;
    x: number;
    y: number;
    w: number;
    h: number;
  };
}

// TFT 아이템 관련 타입
export interface TftItem {
  id: string;
  name: string;
  image: {
    full: string;
    sprite: string;
    group: string;
    x: number;
    y: number;
    w: number;
    h: number;
  };
}

// TFT 특성 관련 타입
export interface TftTrait {
  id: string;
  name: string;
  image: {
    full: string;
    sprite: string;
    group: string;
    x: number;
    y: number;
    w: number;
    h: number;
  };
}

// 특성 단계 정보
export interface TftTraitSet {
  min: number; // 최소 발동 수
  max: number; // 최대 발동 수
  style: number; // 시각적 스타일 (1: 브론즈, 2: 실버, 3: 골드, 4: 크로마틱)
  effects: {
    // 단계별 효과
    [key: string]: number | string;
  };
}
