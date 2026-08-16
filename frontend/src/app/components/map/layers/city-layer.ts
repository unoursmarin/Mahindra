import type { Map as MaplibreMap } from 'maplibre-gl';
import type { FeatureCollection } from 'geojson';
import type { City } from '../../../models/city.model';

const SOURCE_ID = 'cities';
const CIRCLES_LAYER_ID = 'city-circles';
const LABELS_LAYER_ID = 'city-labels';

export function addCitySource(map: MaplibreMap, geojson: FeatureCollection): void {
  map.addSource(SOURCE_ID, {
    type: 'geojson',
    data: geojson,
    cluster: true,
    clusterRadius: 40,
    clusterMaxZoom: 6,
  });
}

export function addCityLayers(map: MaplibreMap): void {
  map.addLayer({
    id: CIRCLES_LAYER_ID,
    type: 'circle',
    source: SOURCE_ID,
    filter: ['!', ['has', 'point_count']],
    paint: {
      'circle-radius': ['interpolate', ['linear'], ['zoom'], 2, 3, 8, 6],
      'circle-color': '#4ade80',
      'circle-opacity': 0.8,
      'circle-stroke-width': 1,
      'circle-stroke-color': '#ffffff',
    },
  });

  map.addLayer({
    id: 'city-clusters',
    type: 'circle',
    source: SOURCE_ID,
    filter: ['has', 'point_count'],
    paint: {
      'circle-color': '#4ade80',
      'circle-opacity': 0.6,
      'circle-radius': ['step', ['get', 'point_count'], 10, 10, 15, 50, 20],
      'circle-stroke-width': 1,
      'circle-stroke-color': '#ffffff',
    },
  });

  map.addLayer({
    id: LABELS_LAYER_ID,
    type: 'symbol',
    source: SOURCE_ID,
    filter: ['!', ['has', 'point_count']],
    minzoom: 5,
    layout: {
      'text-field': ['get', 'name'],
      'text-size': 10,
      'text-offset': [0, 1.2],
      'text-anchor': 'top',
      'text-allow-overlap': false,
    },
    paint: {
      'text-color': '#4ade80',
      'text-halo-color': '#0a0e17',
      'text-halo-width': 1.5,
    },
  });
}

export function clearCities(map: MaplibreMap): void {
  for (const layerId of [LABELS_LAYER_ID, CIRCLES_LAYER_ID, 'city-clusters']) {
    if (map.getLayer(layerId)) {
      map.removeLayer(layerId);
    }
  }
  if (map.getSource(SOURCE_ID)) {
    map.removeSource(SOURCE_ID);
  }
}

export function cityFromFeature(
  properties: Record<string, unknown>,
  coordinates: [number, number],
): City {
  return {
    id: properties['id'] as number,
    name: properties['name'] as string,
    countryId: properties['countryId'] as number,
    population: (properties['population'] as number) ?? null,
    zipCode: (properties['zipCode'] as string) ?? null,
    description: (properties['description'] as string) ?? null,
    latitude: coordinates[1],
    longitude: coordinates[0],
    stateCode: (properties['stateCode'] as string) ?? null,
    stateName: (properties['stateName'] as string) ?? null,
  };
}

export { CIRCLES_LAYER_ID, LABELS_LAYER_ID };
