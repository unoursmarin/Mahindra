import { Component, ViewChild, signal } from '@angular/core';
import { MapComponent } from './components/map/map.component';
import { CountryInfoComponent } from './components/country-info/country-info.component';
import { CityInfoComponent } from './components/city-info/city-info.component';
import { CitiesListComponent } from './components/cities-list/cities-list.component';
import { City } from './models/city.model';
import { Country } from './models/country.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MapComponent, CountryInfoComponent, CityInfoComponent, CitiesListComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  @ViewChild(MapComponent) private mapRef!: MapComponent;

  selectedCountry = signal<Country | null>(null);
  selectedCity = signal<City | null>(null);
  cities = signal<City[]>([]);
  citiesLoading = signal(false);

  onCountrySelected(country: Country | null): void {
    this.selectedCountry.set(country);
    this.selectedCity.set(null);
    if (country === null) {
      this.cities.set([]);
      this.citiesLoading.set(false);
    } else {
      this.citiesLoading.set(true);
    }
  }

  onCitySelected(city: City | null): void {
    this.selectedCity.set(city);
  }

  onCitiesLoaded(cities: City[]): void {
    this.cities.set(cities);
    this.citiesLoading.set(false);
  }

  resetToGlobe(): void {
    this.mapRef.resetView();
  }
}
