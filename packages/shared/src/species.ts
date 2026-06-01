/**
 * Starter bonsai species catalog (Davao-first, tropical-leaning).
 *
 * This is reference data that must stay identical across web and mobile. It is
 * consumed directly by `apps/web` and *mirrored* into `apps/mobile` as a bundled
 * asset (`app/src/main/assets/species.json`) — keep the two in sync.
 *
 * It is a starter list, not the full species browser; grow it as the collection
 * (and the community) does.
 */

/** A bonsai-suitable species in the starter catalog. */
export interface Species {
  /** Everyday name shown in the picker (e.g. "Chinese Elm"). */
  commonName: string;
  /** Botanical name (e.g. "Ulmus parvifolia"). */
  scientificName: string;
  /** Botanical family (e.g. "Ulmaceae"). */
  family: string;
  /** Rough growth/care grouping (e.g. "Tropical ficus", "Conifer bonsai"). */
  bonsaiType: string;
}

export const BONSAI_SPECIES: readonly Species[] = [
  { commonName: "Argao Taiwan", scientificName: "Premna microphylla", family: "Lamiaceae", bonsaiType: "Tropical broadleaf evergreen" },
  { commonName: "Red Balete", scientificName: "Ficus elastica", family: "Moraceae", bonsaiType: "Tropical ficus" },
  { commonName: "Balete", scientificName: "Ficus indica", family: "Moraceae", bonsaiType: "Tropical ficus" },
  { commonName: "Chinese Banyan", scientificName: "Ficus microcarpa", family: "Moraceae", bonsaiType: "Tropical ficus" },
  { commonName: "Weeping Fig", scientificName: "Ficus benjamina", family: "Moraceae", bonsaiType: "Tropical ficus" },
  { commonName: "Ginseng Ficus", scientificName: "Ficus microcarpa 'Ginseng'", family: "Moraceae", bonsaiType: "Tropical ficus cultivar" },
  { commonName: "Tiger Bark Ficus", scientificName: "Ficus microcarpa 'Tiger Bark'", family: "Moraceae", bonsaiType: "Tropical ficus cultivar" },
  { commonName: "Brush Cherry", scientificName: "Syzygium paniculatum", family: "Myrtaceae", bonsaiType: "Tropical broadleaf evergreen" },
  { commonName: "Fukien Tea", scientificName: "Ehretia microphylla", family: "Boraginaceae", bonsaiType: "Tropical broadleaf evergreen" },
  { commonName: "Bougainvillea", scientificName: "Bougainvillea glabra", family: "Nyctaginaceae", bonsaiType: "Flowering tropical bonsai" },
  { commonName: "Jade Plant", scientificName: "Crassula ovata", family: "Crassulaceae", bonsaiType: "Succulent bonsai" },
  { commonName: "Portulacaria Afra", scientificName: "Portulacaria afra", family: "Didiereaceae", bonsaiType: "Succulent bonsai" },
  { commonName: "Juniper", scientificName: "Juniperus chinensis", family: "Cupressaceae", bonsaiType: "Conifer bonsai" },
  { commonName: "Japanese Black Pine", scientificName: "Pinus thunbergii", family: "Pinaceae", bonsaiType: "Conifer bonsai" },
  { commonName: "Japanese Maple", scientificName: "Acer palmatum", family: "Sapindaceae", bonsaiType: "Deciduous bonsai" },
  { commonName: "Chinese Elm", scientificName: "Ulmus parvifolia", family: "Ulmaceae", bonsaiType: "Deciduous bonsai" },
  { commonName: "Serissa", scientificName: "Serissa japonica", family: "Rubiaceae", bonsaiType: "Flowering bonsai" },
  { commonName: "Wrightia", scientificName: "Wrightia religiosa", family: "Apocynaceae", bonsaiType: "Tropical flowering bonsai" },
  { commonName: "Tamarind", scientificName: "Tamarindus indica", family: "Fabaceae", bonsaiType: "Tropical broadleaf bonsai" },
  { commonName: "Rain Tree", scientificName: "Samanea saman", family: "Fabaceae", bonsaiType: "Tropical broadleaf bonsai" },
];
