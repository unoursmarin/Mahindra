import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Country } from '../../models/country.model';

@Component({
  selector: 'app-country-info',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './country-info.component.html',
  styleUrl: './country-info.component.scss',
})
export class CountryInfoComponent {
  country = input<Country | null>(null);
  close = output<void>();

  onClose(): void {
    this.close.emit();
  }

  formatPopulation(pop: number | null): string {
    if (pop == null) return '—';
    return new Intl.NumberFormat('fr-FR').format(pop);
  }
}
