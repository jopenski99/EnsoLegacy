export interface TimelineEvent {
  year: string;
  isPresent?: boolean;
  isPremium?: boolean;
  title: string;
  description: string;
}

export interface GalleryImage {
  src: string;
  alt: string;
  isPrimary?: boolean;
}

export interface Steward {
  name: string;
  isCurrent?: boolean;
  isPlaceholder?: boolean;
}

export interface TreeHeritage {
  name: string;
  species: string;
  currentExhibition: string;
  ancestralValue: string;
  heroImage: string;
  heroAlt: string;
  gallery: GalleryImage[];
  timeline: TimelineEvent[];
  stewards: Steward[];
}

export const sentinelData: TreeHeritage = {
  name: "The Sentinel",
  species: "Juniperus procumbens 'Nana'",
  currentExhibition: "Current Exhibition State: Spring 2024",
  ancestralValue: "Priceless Heirloom",
  heroImage:
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCDRKpJ-6GZ4PNx5IScR4vdl_3LBLB-WdW9I1SBlsTbX1O1UyrFOVpgXZX91DyVpHfL3B3ujXAPivvIF6mqJbHK3GA4hchhyvH-V1DQ-E-s_O-P0Pl0neb1rzoEAAoC6uq7ISd6bokUuudm2Kdyxp2eMFBIrY5BNrUA6g_R4Wtl-FuFxa5RoGBSdzFjPDF8Ag362rj4Vn3YoNqEJPGoOns-Cc_NL2ajzv5OVRe2nIfO-Dk6jX60IdIguxsiH3RWErF9HOG8ZUfroQ4",
  heroAlt:
    "A cinematic, high-resolution photograph of a centenarian Juniper bonsai tree, 'The Sentinel', staged in a minimalist Japanese garden. The tree features dramatic, bleached shari deadwood and dense, meticulously pruned emerald foliage pads. The lighting is soft morning sun from the side, casting gentle shadows against a cream-colored textured wall. The style is premium editorial photography with a soft, warm, light-mode palette and organic textures.",
  gallery: [
    {
      src: "https://lh3.googleusercontent.com/aida-public/AB6AXuC0CY2FLjNBuEASHdQaA6Le90D3LdWTfWmHCM8gB0bpVV0-oCpZ0gaMWIa63_Ssl8tnEYaZYP36QvOw8wyX3hf0lha7IqXSnsHM4mRtaNOkcpxZ5v59JWHju8qQNSKheuS9vVr5jIzIk-SOYWU824zqRayg17BBjhYk0sZsVhSAw3jhFW2Ohc8tt2uXNiK7ZSDrHli-gABBV-fO5YI8CvAXMMt1IeIm-ytgiIb8ImnWyKFEDum3lAku8h74x4ds4WB9khhegBtQquQ",
      alt: "A close-up macro photograph of a bonsai's gnarled trunk and weathered bark, showing years of aging and character. The wood texture is rich with detail, lit by soft natural light to emphasize the deep grooves and silvered deadwood. The color palette is earth tones of brown, beige, and slate grey, set against a blurred warm background.",
    },
    {
      src: "https://lh3.googleusercontent.com/aida-public/AB6AXuDO5Y3_xse0GB5RlBWQbvLAoCfSKnJFLiYjcQu1cxEE-zU8S7FSSh8o3h-Fr908kxXRscsCjDsCprbW62D4cDKU9B1aKW57CW3HtPH4Yd_get_H7hpe5-zHBFlO6IrujCeh03w_dZ89hq7tDV8TLaIH2Br9R0uAkD7dbhFSgQ2z28sLWNj9IBxEGMEsVLl_4FAyx5z8P-nPOphKBcBNCf_HkFZgmnqlCNocv4ZeNaBjZyJLGmpzsuChwKXYJXOExHvzBnKGQWskGHqBw",
      alt: "A detailed view of lush green bonsai foliage pads under bright, diffused daylight. The needles are short, dense, and perfectly healthy, displaying a vibrant green hue with hints of gold at the tips. The composition is tight and focused, highlighting the cloud-like structure of the canopy against a soft, neutral-toned background.",
    },
    {
      src: "https://lh3.googleusercontent.com/aida-public/AB6AXuCP523VRQiMEolLid084bblFzhaJuB4bv0mspNhyT29F_x_klrsr51S3NFT6bFD_bWB-4hM0rLiBsP_LsL3y-pofCYufbMitzKU926e8bc_M706DLIESX0MO1RHFXvj1uWbXIhhM2EvIKBecYsy6zm3MPLbhS2RaK1GX1256yTUNPAFBjvQehaXqwghBaHlXCDGV98P0KjCvTbnltLiBZ8hIwNkpXKL5Lq9Y6M5tkD5xmBJSEmeMJlPiw47IChs2FJvNHrroaPZP2E",
      alt: "A seasonal photograph of a bonsai tree during a cold snap, with subtle frost patterns on the glazed ceramic pot and lower branches. The atmosphere is quiet and serene, using cool blue-toned light and pale whites to represent winter. The tree remains dormant but maintains its majestic structural form under the soft morning light.",
    },
  ],
  timeline: [
    {
      year: "1924",
      title: "The Awakening",
      description:
        "Collected as a wild specimen in the rocky crevices of the Kii Peninsula. The initial form was weathered by ocean winds, establishing the deadwood shari that defines its current spirit.",
    },
    {
      year: "1948",
      title: "Structure of Resilience",
      description:
        "First potting in a heavy Yixing clay vessel. Training focused on the primary branch structure, utilizing traditional copper wire techniques to guide the movement downward.",
    },
    {
      year: "1982",
      title: "Refinement Pruning",
      description:
        "A significant structural pruning was conducted to increase light penetration to the inner canopy. The foliage pads began to take on a cloud-like maturity.",
    },
    {
      year: "2023 - Present",
      isPresent: true,
      isPremium: true,
      title: "The Third Stewardship",
      description:
        "Officially transferred to the Enso Legacy collection. Current maintenance focuses on seasonal defoliation and soil oxygenation to preserve the centennial root system.",
    },
  ],
  stewards: [
    { name: "S. Tanaka" },
    { name: "H. Mori" },
    { name: "Current", isCurrent: true },
  ],
};
