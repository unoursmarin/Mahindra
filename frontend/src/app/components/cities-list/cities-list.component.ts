import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { City } from '../../models/city.model';

@Component({
  selector: 'app-cities-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cities-list.component.html',
  styleUrl: './cities-list.component.scss',
})
export class CitiesListComponent {
  cities = input<City[]>([]);
  countryName = input<string | null>(null);
  visible = input<boolean>(false);
  isLoading = input<boolean>(false);

  cityClicked = output<City>();

  onCityClick(city: City): void {
    this.cityClicked.emit(city);
  }

  formatCoord(value: number | null): string {
    if (value == null) return '—';
    return value.toFixed(2);
  }
}
