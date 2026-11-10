import { Component, Input, Output } from "@angular/core";
import { MetaResponse } from "../../../../model/IMetas.models";
import { ProgressBarModule } from "primeng/progressbar";
import { AppMoedaPipe } from "../../../../shared/pipes/app-moeda.pipe";

@Component({
  selector: 'app-meta-item-dashboard',
  imports: [ProgressBarModule, AppMoedaPipe],
  templateUrl: './meta-item-dashboard.html',
})

export class MetaItemDashboardComponent {

  @Input() meta!: MetaResponse;

  get valorFalta(): number { return this.meta.valorTotal - this.meta.valorAtual!; }
}
