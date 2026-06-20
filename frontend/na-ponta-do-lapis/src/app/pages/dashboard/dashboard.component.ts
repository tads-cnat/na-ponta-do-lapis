import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  template: `
    <div class="bg-[#F5F6FA] p-6 2xl:p-8 h-[calc(100vh-64px)] overflow-y-auto overflow-x-hidden">
      <h1>Top 10 Dashboards</h1>
    </div>

  `,
})
export class DashboardComponent { }
