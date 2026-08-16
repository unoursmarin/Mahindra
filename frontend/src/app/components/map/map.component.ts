import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  inject,
  output,
  signal,
} from '@angular/core';
import { Map as MaplibreMap, MapMouseEvent, NavigationControl, setWorkerUrl } from 'maplibre-gl';
import { Subscription } from 'rxjs';
import { City } from '../../models/city.model';
import { Country } from '../../models/country.model';
import { GeoApiService } from '../../services/geo-api.service';
import {
  LABELS_LAYER_ID as COUNTRY_LABELS,
  DETECTION_LAYER_ID,
  addCountryLayers,
  addCountrySource,
  buildCountryLookup,
  countryFromFeature,
  ensureBoundarySource,
  highlightCountry,
} from './layers/country-layer';
import {
  CIRCLES_LAYER_ID as CITY_CIRCLES,
  LABELS_LAYER_ID as CITY_LABELS,
  addCityLayers,
  addCitySource,
  cityFromFeature,
  clearCities,
} from './layers/city-layer';
import { buildGlobeStyle } from './layers/globe-style';

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss',
})
export class MapComponent implements OnInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  countrySelected = output<Country | null>();
  citySelected = output<City | null>();
  citiesLoaded = output<City[]>();
  isLoading = signal(true);

  private map!: MaplibreMap;
  private geoApiService = inject(GeoApiService);
  private citySub: Subscription | null = null;
  private selectedCountryId: number | null = null;
  private countryByCca2 = new Map<string, Country>();

  private readonly CITY_ZOOM_THRESHOLD = 4;
  private readonly GLOBE_ZOOM_THRESHOLD = 2.5;

  ngOnInit(): void {
    setWorkerUrl('/assets/maplibre-gl-worker.mjs');

    this.map = new MaplibreMap({
      container: this.mapContainer.nativeElement,
      style: buildGlobeStyle(),
      center: [0, 20],
      zoom: 1.5,
    });

    this.map.addControl(new NavigationControl(), 'bottom-right');

    this.map.on('load', () => {
      this.map.setProjection({ type: 'globe' });
      this.map.setSky({
        'sky-color': '#0a0e17',
        'sky-horizon-blend': 0.5,
        'horizon-color': '#0a1628',
        'horizon-fog-blend': 0.1,
        'atmosphere-blend': ['interpolate', ['linear'], ['zoom'], 0, 1, 7, 0] as unknown as number,
      });

      this.geoApiService.getCountriesGeoJson().subscribe({
        next: (geojson) => {
          addCountrySource(this.map, geojson);
          addCountryLayers(this.map);
          ensureBoundarySource(this.map);
          this.countryByCca2 = buildCountryLookup(geojson);
          this.registerCountryInteractions();
          this.registerGlobalClickHandler();
          this.registerZoomHandler();
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        },
      });
    });
  }

  ngOnDestroy(): void {
    this.citySub?.unsubscribe();
    this.map?.remove();
  }

  /** Resets the globe to the default view (zoom out, deselect country). */
  resetView(): void {
    this.selectCountry(null);
    this.map.flyTo({ center: [0, 20], zoom: 1.5, duration: 1500 });
  }

  private registerZoomHandler(): void {
    this.map.on('zoomend', () => {
      const zoom = this.map.getZoom();

      if (zoom < this.GLOBE_ZOOM_THRESHOLD) {
        this.selectCountry(null);
        return;
      }

      if (zoom < this.CITY_ZOOM_THRESHOLD) return;

      // Detection layer may not be ready yet (boundary GeoJSON still fetching)
      if (!this.map.getLayer(DETECTION_LAYER_ID)) return;

      const center = this.map.project(this.map.getCenter());
      const features = this.map.queryRenderedFeatures(center, { layers: [DETECTION_LAYER_ID] });
      const feature = features[0];
      if (!feature) return;

      const iso2 = (feature.properties?.['ISO_A2'] as string | undefined) ?? '';
      if (!iso2 || iso2 === '-99') return; // -99 = disputed territory in Natural Earth data

      const country = this.countryByCca2.get(iso2.toUpperCase());
      if (!country) return;

      this.selectCountry(country, false);
    });
  }

  private registerGlobalClickHandler(): void {
    this.map.on('click', (e: MapMouseEvent) => {
      if (this.map.getZoom() >= this.CITY_ZOOM_THRESHOLD) return;

      const features = this.map.queryRenderedFeatures(e.point, {
        layers: [COUNTRY_LABELS, CITY_CIRCLES],
      });
      if (features.length === 0) {
        this.selectCountry(null);
      }
    });
  }

  private registerCountryInteractions(): void {
    this.map.on('click', COUNTRY_LABELS, (e) => {
      const feature = e.features?.[0];
      if (!feature) return;

      const coords = (feature.geometry as GeoJSON.Point).coordinates as [number, number];
      const country = countryFromFeature(feature.properties as Record<string, unknown>, coords);
      this.selectCountry(country);
    });

    this.map.on('mouseenter', COUNTRY_LABELS, () => {
      this.map.getCanvas().style.cursor = 'pointer';
    });
    this.map.on('mouseleave', COUNTRY_LABELS, () => {
      this.map.getCanvas().style.cursor = '';
    });
  }

  private registerCityInteractions(): void {
    this.map.on('click', CITY_CIRCLES, (e) => {
      e.preventDefault();
      const feature = e.features?.[0];
      if (!feature) return;
      const coords = (feature.geometry as GeoJSON.Point).coordinates as [number, number];
      const city = cityFromFeature(feature.properties as Record<string, unknown>, coords);
      this.citySelected.emit(city);
    });

    this.map.on('click', CITY_LABELS, (e) => {
      e.preventDefault();
      const feature = e.features?.[0];
      if (!feature) return;
      const coords = (feature.geometry as GeoJSON.Point).coordinates as [number, number];
      const city = cityFromFeature(feature.properties as Record<string, unknown>, coords);
      this.citySelected.emit(city);
    });

    this.map.on('mouseenter', CITY_CIRCLES, () => {
      this.map.getCanvas().style.cursor = 'pointer';
    });
    this.map.on('mouseleave', CITY_CIRCLES, () => {
      this.map.getCanvas().style.cursor = '';
    });
  }

  private selectCountry(country: Country | null, flyTo = true): void {
    if (country !== null && country.id === this.selectedCountryId) return;

    this.selectedCountryId = country?.id ?? null;
    highlightCountry(this.map, country);
    this.countrySelected.emit(country);
    this.citySelected.emit(null);

    clearCities(this.map);

    if (country === null) return;

    if (flyTo) {
      this.map.flyTo({
        center: [country.capitalLng ?? 0, country.capitalLat ?? 0],
        zoom: 4,
        duration: 1500,
      });
    }

    this.citySub?.unsubscribe();
    this.citySub = this.geoApiService.getCitiesGeoJson(country.id).subscribe({
      next: (geojson) => {
        clearCities(this.map);
        addCitySource(this.map, geojson);
        addCityLayers(this.map);
        this.registerCityInteractions();

        // Extract structured City objects from the GeoJSON to populate the cities list
        const cities: City[] = geojson.features
          .filter((f) => f.geometry?.type === 'Point')
          .map((f) =>
            cityFromFeature(
              f.properties as Record<string, unknown>,
              (f.geometry as GeoJSON.Point).coordinates as [number, number],
            ),
          );
        this.citiesLoaded.emit(cities);
      },
      error: () => {
        this.citiesLoaded.emit([]);
      },
    });
  }
}
