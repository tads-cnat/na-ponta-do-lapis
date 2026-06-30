import { Component, Input, Output } from "@angular/core";
import { MetaResponse } from "../../../../model/IMetas.models";
import { ProgressBarModule } from "primeng/progressbar";

@Component({
  selector: 'app-meta-item-dashboard',
  imports: [ProgressBarModule],
  templateUrl: './meta-item-dashboard.html',
})

export class MetaItemDashboardComponent {

  @Input() meta!: MetaResponse;

  get valorFalta(): number { return this.meta.valorTotal - this.meta.valorAtual!; }
}
