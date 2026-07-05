import { Component, inject, Input, ChangeDetectorRef } from "@angular/core";
import { CurrencyPipe, DecimalPipe } from "@angular/common";
import { IContas } from "../../../../model/IContas.models";
import { ContaFinanceiraService } from "../../../contas/service/contas.service";
import { MessageService } from "primeng/api";
import { TransacoesService } from "../../../transacoes/service/transacoes.service";
import { ITransacoes } from "../../../../model/ITransacoes.model";
import { MetasService } from "../../../metas/services/metas.service";
import { MetaResponse } from './../../../../model/IMetas.models';
import { ICotacao } from "../../../../model/ICotacao.models";

@Component({
  selector: 'app-cards-resumo',
  imports: [CurrencyPipe, DecimalPipe],
  template: `
    <section class="grid grid-cols-1 lg:grid-cols-4 gap-4">

      <!-- Card: Saldo atual -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Saldo atual</span>
          <div class="w-12 h-12 rounded-full bg-purple-100 flex items-center justify-center">
            <!-- ícone placeholder -->
            <span class="text-purple-400 text-lg">
              <svg width="32" height="24" viewBox="0 0 32 24" fill="none" xmlns="http://www.w3.org/2000/svg">
<path opacity="0.587821" d="M23.9996 6.66699C26.2088 6.66699 27.9996 8.45785 27.9996 10.667C27.9995 12.876 26.2087 14.667 23.9996 14.667C21.7908 14.6668 19.9998 12.8759 19.9996 10.667C19.9996 8.45796 21.7906 6.66717 23.9996 6.66699ZM11.9996 0C14.945 0 17.3334 2.38764 17.3336 5.33301C17.3336 8.27853 14.9452 10.667 11.9996 10.667C9.05426 10.6668 6.66663 8.27842 6.66663 5.33301C6.6668 2.38775 9.05437 0.000175999 11.9996 0Z" fill="#8280FF"/>
<path fill-rule="evenodd" clip-rule="evenodd" d="M11.9774 13.3335C18.3611 13.3335 23.6061 16.391 23.997 22.9331C24.0125 23.1937 23.9966 24.0005 22.995 24.0005H0.969619C0.635144 24.0001 -0.0272745 23.2787 0.000869049 22.9321C0.517816 16.5688 5.68241 13.3336 11.9774 13.3335ZM23.4686 16.0024C28.0103 16.0523 31.7187 18.3471 31.9979 23.1997C32.0092 23.3952 31.9977 24.0005 31.2743 24.0005H26.1337C26.1337 20.9998 25.1416 18.2305 23.4686 16.0024Z" fill="#8280FF"/>
</svg>

            </span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">{{ saldoTotal | currency : 'BRL' }}</span>
      </div>

      <!-- Card: Moeda -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Moeda</span>
          <div class="w-12 h-12 rounded-full bg-yellow-100 flex items-center justify-center">
            <span class="text-yellow-500 text-lg"><svg width="34" height="34" viewBox="0 0 34 34" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M24.375 11.5835V10.8438C24.375 7.51281 19.3507 5 12.6875 5C6.0243 5 1 7.51281 1 10.8438V16.1562C1 18.9307 4.48633 21.1354 9.5 21.7955V22.5312C9.5 25.8622 14.5243 28.375 21.1875 28.375C27.8507 28.375 32.875 25.8622 32.875 22.5312V17.2188C32.875 14.4695 29.4989 12.2622 24.375 11.5835ZM30.75 17.2188C30.75 18.9745 26.6607 20.9375 21.1875 20.9375C20.6921 20.9375 20.2007 20.9202 19.7159 20.8884C22.5807 19.8445 24.375 18.1484 24.375 16.1562V13.7311C28.3421 14.3221 30.75 15.9265 30.75 17.2188ZM9.5 19.6426V16.483C10.5569 16.6207 11.6217 16.689 12.6875 16.6875C13.7533 16.689 14.8181 16.6207 15.875 16.483V19.6426C14.8197 19.7985 13.7543 19.8762 12.6875 19.875C11.6207 19.8762 10.5553 19.7985 9.5 19.6426ZM22.25 14.2876V16.1562C22.25 17.2705 20.6018 18.4672 18 19.1937V16.0898C19.7146 15.6741 21.1663 15.0552 22.25 14.2876ZM12.6875 7.125C18.1607 7.125 22.25 9.08797 22.25 10.8438C22.25 12.5995 18.1607 14.5625 12.6875 14.5625C7.2143 14.5625 3.125 12.5995 3.125 10.8438C3.125 9.08797 7.2143 7.125 12.6875 7.125ZM3.125 16.1562V14.2876C4.20875 15.0552 5.66039 15.6741 7.375 16.0898V19.1937C4.7732 18.4672 3.125 17.2705 3.125 16.1562ZM11.625 22.5312V21.9774C11.9743 21.9907 12.3276 22 12.6875 22C13.2028 22 13.7062 21.9827 14.2002 21.9535C14.7491 22.15 15.3082 22.3167 15.875 22.4529V25.5687C13.2732 24.8422 11.625 23.6455 11.625 22.5312ZM18 26.0176V22.85C19.0566 22.992 20.1214 23.063 21.1875 23.0625C22.2533 23.064 23.3181 22.9957 24.375 22.858V26.0176C22.2613 26.3275 20.1137 26.3275 18 26.0176ZM26.5 25.5687V22.4648C28.2146 22.0491 29.6663 21.4302 30.75 20.6626V22.5312C30.75 23.6455 29.1018 24.8422 26.5 25.5687Z" fill="#FEC436"/>
</svg>
</span>
          </div>
        </div>
<div class="
    flex
    flex-1
    items-end
    justify-center
    pb-2
">

    <div class="
        flex
        items-center
        justify-center
        gap-10
    ">

            @if (cotacaoDolar; as dolar) {

                <div class="flex flex-col items-center gap-1">

                  <div class="flex items-center gap-1">
                      USD
                      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M17.7225 3.5C17.376 2.9045 16.7385 2.5 16 2.5H9V3.5H17.7225ZM0 12.5H18V13.5H0V12.5ZM9 8.5H18V9.5H9V8.5ZM9 6.5H18V7.5H9V6.5ZM0 10.5H18V11.5H0V10.5ZM2 15.5H16C16.7385 15.5 17.376 15.0955 17.7225 14.5H0.2775C0.624 15.0955 1.2615 15.5 2 15.5ZM9 4.5H18V5.5H9V4.5Z" fill="#B22334"/>
<path d="M0.034 13.8395C0.0423333 13.8862 0.0521667 13.9323 0.0635 13.978C0.0768333 14.028 0.0921667 14.0773 0.1095 14.126C0.154 14.2555 0.208 14.3805 0.276 14.4975L0.2775 14.5H17.7225L17.7235 14.498C17.7913 14.3805 17.8469 14.2563 17.8895 14.1275C17.922 14.0335 17.9474 13.9373 17.9655 13.8395C17.986 13.7295 18 13.6165 18 13.5H0C0 13.6165 0.014 13.729 0.034 13.8395ZM0 11.5H18V12.5H0V11.5ZM0 9.5V10.5H18V9.5H9H0ZM9 7.5H18V8.5H9V7.5ZM9 5.5H18V6.5H9V5.5ZM0.064 4.022C0.0765 3.971 0.094 3.9225 0.11 3.8735C0.0927479 3.92239 0.077403 3.97192 0.064 4.022ZM9 4.5H18C18 4.3835 17.986 4.2705 17.9655 4.16C17.948 4.06205 17.9224 3.96572 17.889 3.872C17.8463 3.74265 17.7905 3.61801 17.7225 3.5H9V4.5Z" fill="#EEEEEE"/>
<path d="M9 2.5H2C1.46957 2.5 0.960859 2.71071 0.585786 3.08579C0.210714 3.46086 0 3.96957 0 4.5L0 9.5H9V2.5Z" fill="#3C3B6E"/>
<path d="M1.00049 3.863L1.30949 4.0875L1.19149 4.45L1.49999 4.226L1.80899 4.45L1.69099 4.0875L1.99999 3.863H1.61799L1.49999 3.5L1.38249 3.863H1.00049ZM2.00049 4.863L2.30949 5.0875L2.19149 5.45L2.49999 5.226L2.80899 5.45L2.69099 5.0875L2.99999 4.863H2.61799L2.49999 4.5L2.38249 4.863H2.00049ZM4.00049 4.863L4.30949 5.0875L4.19149 5.45L4.49999 5.226L4.80899 5.45L4.69099 5.0875L4.99999 4.863H4.61799L4.49999 4.5L4.38249 4.863H4.00049ZM6.00049 4.863L6.30949 5.0875L6.19149 5.45L6.49999 5.226L6.80899 5.45L6.69099 5.0875L6.99999 4.863H6.61799L6.49999 4.5L6.38249 4.863H6.00049ZM2.00049 6.863L2.30949 7.0875L2.19149 7.45L2.49999 7.226L2.80899 7.45L2.69099 7.0875L2.99999 6.863H2.61799L2.49999 6.5L2.38249 6.863H2.00049ZM4.00049 6.863L4.30949 7.0875L4.19149 7.45L4.49999 7.226L4.80899 7.45L4.69099 7.0875L4.99999 6.863H4.61799L4.49999 6.5L4.38249 6.863H4.00049ZM6.00049 6.863L6.30949 7.0875L6.19149 7.45L6.49999 7.226L6.80899 7.45L6.69099 7.0875L6.99999 6.863H6.61799L6.49999 6.5L6.38249 6.863H6.00049ZM3.00049 3.863L3.30949 4.0875L3.19149 4.45L3.49999 4.226L3.80899 4.45L3.69099 4.0875L3.99999 3.863H3.61799L3.49999 3.5L3.38249 3.863H3.00049ZM5.00049 3.863L5.30949 4.0875L5.19149 4.45L5.49999 4.226L5.80899 4.45L5.69099 4.0875L5.99999 3.863H5.61799L5.49999 3.5L5.38249 3.863H5.00049ZM7.00049 3.863L7.30949 4.0875L7.19149 4.45L7.49999 4.226L7.80899 4.45L7.69099 4.0875L7.99999 3.863H7.61799L7.49999 3.5L7.38249 3.863H7.00049ZM1.00049 5.863L1.30949 6.0875L1.19149 6.45L1.49999 6.226L1.80899 6.45L1.69099 6.0875L1.99999 5.863H1.61799L1.49999 5.5L1.38249 5.863H1.00049ZM3.19149 6.45L3.49999 6.226L3.80899 6.45L3.69099 6.0875L3.99999 5.863H3.61799L3.49999 5.5L3.38249 5.863H3.00049L3.30949 6.0875L3.19149 6.45ZM5.00049 5.863L5.30949 6.0875L5.19149 6.45L5.49999 6.226L5.80899 6.45L5.69099 6.0875L5.99999 5.863H5.61799L5.49999 5.5L5.38249 5.863H5.00049ZM7.00049 5.863L7.30949 6.0875L7.19149 6.45L7.49999 6.226L7.80899 6.45L7.69099 6.0875L7.99999 5.863H7.61799L7.49999 5.5L7.38249 5.863H7.00049ZM1.00049 7.863L1.30949 8.0875L1.19149 8.45L1.49999 8.226L1.80899 8.45L1.69099 8.0875L1.99999 7.863H1.61799L1.49999 7.5L1.38249 7.863H1.00049ZM3.19149 8.45L3.49999 8.226L3.80899 8.45L3.69099 8.0875L3.99999 7.863H3.61799L3.49999 7.5L3.38249 7.863H3.00049L3.30949 8.0875L3.19149 8.45ZM5.00049 7.863L5.30949 8.0875L5.19149 8.45L5.49999 8.226L5.80899 8.45L5.69099 8.0875L5.99999 7.863H5.61799L5.49999 7.5L5.38249 7.863H5.00049ZM7.00049 7.863L7.30949 8.0875L7.19149 8.45L7.49999 8.226L7.80899 8.45L7.69099 8.0875L7.99999 7.863H7.61799L7.49999 7.5L7.38249 7.863H7.00049Z" fill="white"/>
</svg>
            </div>

                    <span class="font-semibold text-gray-800">

                        R$
                        {{ dolar.compra | number:'1.2-2' }}

                    </span>

                </div>

            }

            @if (cotacaoEuro; as euro) {

                <div class="flex flex-col items-center gap-1">
                  <div class="flex items-center gap-1">

                        EUR
                        <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M16 2.5H2C1.46957 2.5 0.960859 2.71071 0.585786 3.08579C0.210714 3.46086 0 3.96957 0 4.5L0 13.5C0 14.0304 0.210714 14.5391 0.585786 14.9142C0.960859 15.2893 1.46957 15.5 2 15.5H16C16.5304 15.5 17.0391 15.2893 17.4142 14.9142C17.7893 14.5391 18 14.0304 18 13.5V4.5C18 3.96957 17.7893 3.46086 17.4142 3.08579C17.0391 2.71071 16.5304 2.5 16 2.5Z" fill="#003399"/>
<path d="M9.26956 4.85242L9.69406 4.54392H9.16956L9.00706 4.04492L8.84506 4.54392H8.32056L8.74506 4.85242L8.58256 5.35142L9.00706 5.04292L9.43156 5.35142L9.26956 4.85242ZM9.26956 13.5189L9.69406 13.2104H9.16956L9.00706 12.7114L8.84506 13.2104H8.32056L8.74506 13.5189L8.58256 14.0179L9.00706 13.7094L9.43156 14.0179L9.26956 13.5189ZM4.93656 9.18542L5.36106 8.87692H4.83606L4.67406 8.37792L4.51156 8.87692H3.98706L4.41156 9.18542L4.24956 9.68442L4.67406 9.37592L5.09856 9.68442L4.93656 9.18542ZM5.49006 7.04292L5.91456 6.73442H5.38956L5.22756 6.23542L5.06556 6.73442H4.54056L4.96506 7.04292L4.80306 7.54192L5.22756 7.23342L5.65206 7.54192L5.49006 7.04292ZM5.49006 11.3524L5.91456 11.0439H5.38956L5.22756 10.5449L5.06556 11.0439H4.54056L4.96506 11.3524L4.80306 11.8514L5.22756 11.5429L5.65206 11.8514L5.49006 11.3524ZM7.10306 5.43292L7.52756 5.12442H7.00256L6.84056 4.62542L6.67856 5.12442H6.15356L6.57806 5.43292L6.41606 5.93192L6.84056 5.62342L7.26506 5.93192L7.10306 5.43292ZM7.10306 12.9664L7.52756 12.6579H7.00256L6.84056 12.1589L6.67856 12.6579H6.15356L6.57806 12.9664L6.41606 13.4654L6.84056 13.1574L7.26506 13.4654L7.10306 12.9664ZM13.0636 9.18542L12.6391 8.87692H13.1641L13.3261 8.37792L13.4886 8.87692H14.0131L13.5886 9.18542L13.7506 9.68442L13.3261 9.37592L12.9016 9.68442L13.0636 9.18542ZM12.5101 7.04292L12.0856 6.73442H12.6106L12.7726 6.23542L12.9346 6.73442H13.4596L13.0351 7.04292L13.1971 7.54192L12.7726 7.23342L12.3481 7.54192L12.5101 7.04292ZM12.5101 11.3524L12.0856 11.0439H12.6106L12.7726 10.5449L12.9346 11.0439H13.4596L13.0351 11.3524L13.1971 11.8514L12.7726 11.5429L12.3481 11.8514L12.5101 11.3524ZM10.8971 5.43292L10.4726 5.12442H10.9976L11.1596 4.62542L11.3216 5.12442H11.8466L11.4221 5.43292L11.5841 5.93192L11.1596 5.62342L10.7351 5.93192L10.8971 5.43292ZM10.8971 12.9664L10.4726 12.6579H10.9976L11.1596 12.1589L11.3216 12.6579H11.8466L11.4221 12.9664L11.5841 13.4654L11.1596 13.1574L10.7351 13.4654L10.8971 12.9664Z" fill="#FFCC00"/>
</svg>

</div>
                    <span class="font-semibold text-gray-800">

                        R$
                        {{ euro.compra | number:'1.2-2' }}

                    </span>

                </div>

            }


            @if (!cotacaoDolar && !cotacaoEuro) {

                <span class="text-gray-400">
                    Carregando cotações...
                </span>

            }
          </div>
          </div>
      </div>
      <!-- Card: Receita -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Receita</span>
          <div class="w-12 h-12 rounded-full bg-green-100 flex items-center justify-center">
            <span class="text-green-500 text-lg"><svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M3.11111 24.8889H26.4444C27.3036 24.8889 28 25.5853 28 26.4444C28 27.3036 27.3036 28 26.4444 28H1.55556C0.696446 28 0 27.3036 0 26.4444V1.55556C0 0.696446 0.696446 0 1.55556 0C2.41467 0 3.11111 0.696446 3.11111 1.55556V24.8889Z" fill="#4AD991"/>
<path opacity="0.5" d="M8.91263 18.175C8.32504 18.8017 7.34063 18.8335 6.71388 18.2459C6.08713 17.6583 6.05537 16.6739 6.64295 16.0472L12.4763 9.82494C13.0445 9.21879 13.9888 9.16623 14.6209 9.70555L19.2249 13.6343L25.2235 6.03606C25.7559 5.36176 26.734 5.24668 27.4083 5.77903C28.0826 6.31137 28.1977 7.28955 27.6654 7.96385L20.6654 16.8305C20.1186 17.5231 19.1059 17.6227 18.4347 17.0499L13.7306 13.0358L8.91263 18.175Z" fill="#4AD991"/>
</svg>
</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">{{ transacoesReceita | currency: 'BRL' }}</span>
      </div>

      <!-- Card: Despesa -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Despesa</span>
          <div class="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
            <span class="text-red-400 text-lg"><svg width="28" height="30" viewBox="0 0 28 30" fill="none" xmlns="http://www.w3.org/2000/svg">
<path opacity="0.78" fill-rule="evenodd" clip-rule="evenodd" d="M12.6312 9.00316C12.6512 8.74266 12.8684 8.5415 13.1297 8.5415H13.5475C13.8044 8.5415 14.0195 8.73615 14.045 8.99175L14.6667 15.2082L19.0814 17.7309C19.2372 17.8199 19.3333 17.9856 19.3333 18.165V18.5535C19.3333 18.8832 19.0199 19.1227 18.7018 19.0359L12.3987 17.3169C12.1673 17.2538 12.0133 17.0353 12.0317 16.7962L12.6312 9.00316Z" fill="#FF9066"/>
<path opacity="0.901274" fill-rule="evenodd" clip-rule="evenodd" d="M5.85254 0.384655C5.94785 -0.0149454 6.45754 -0.135964 6.72168 0.1786L8.52051 2.32313C10.2036 1.60645 12.0552 1.2079 14 1.2079C21.7319 1.2079 27.9998 7.47607 28 15.2079C28 22.9399 21.732 29.2079 14 29.2079C6.26801 29.2079 0 22.9399 0 15.2079C3.22093e-05 13.8949 0.180533 12.6239 0.518555 11.4188L3.08594 12.1395C2.8086 13.1284 2.66702 14.1574 2.66699 15.2079C2.66699 21.4671 7.74077 26.5419 14 26.5419C20.2592 26.5419 25.333 21.4671 25.333 15.2079C25.3328 8.94883 20.2591 3.87489 14 3.87489C12.7318 3.87489 11.4968 4.08334 10.332 4.48133L12.1328 6.62782C12.3974 6.94309 12.1892 7.42483 11.7783 7.44813L4.7334 7.84755C4.39924 7.86638 4.14112 7.5579 4.21875 7.23231L5.85254 0.384655Z" fill="#FF9066"/>
</svg>
</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">{{transacoesDespesa | currency: 'BRL' }}</span>
      </div>

    </section>
  `
})

export class CardsResumoComponent {
  @Input() contas: IContas[] = [];

  @Input() transacoes: ITransacoes[] = [];

  @Input() cotacaoDolar?: ICotacao;

  @Input() cotacaoEuro?: ICotacao;

  @Input() metas: MetaResponse[] = [];

  contaService = inject(ContaFinanceiraService);

  transacaoService = inject(TransacoesService);

  metaService = inject(MetasService);

  saldoTotal: number = 0;

  transacoesReceita: number = 0;

  transacoesDespesa: number = 0;

  private cdr = inject(ChangeDetectorRef);

  private messageService = inject(MessageService)

  ngOnInit(): void {
    this.listarContas();
    this.listarTransacoes();
    this.listarMetas();
  }

  public listarContas(): void {
    this.contaService.listarContasUsuarioLogado().subscribe({
      next: (res: IContas[]) => {
        this.contas = res;
        console.log(this.contas)
        this.saldoTotal = this.transacoesReceita - this.transacoesDespesa;
        this.cdr.detectChanges();
      },
      error: (err:Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados da conta',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
      }
    })
  }

public listarTransacoes(): void {
  this.transacaoService.listarTransacoes().subscribe({
    next: (res: ITransacoes[]) => {
      this.transacoes = res;
      console.log(this.transacoes);
      this.transacoesReceita = 0;
      this.transacoesDespesa = 0;
      res.forEach(atual => {
        if(atual.estado !== 'PENDENTE'){
          if(atual.tipo == 'RECEITA'){
            this.transacoesReceita += atual.valor;
          }
          else {
            this.transacoesDespesa += atual.valor;
          }
        }
        else {
          return;
        }
        this.saldoTotal = this.transacoesReceita - this.transacoesDespesa;
      });
    },
    error: (err: Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados das transações',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
    }
  })
}

public listarMetas(): void {
  this.metaService.listarMetas().subscribe({
    next: (res: MetaResponse[]) => {
      this.metas = res;
      console.log(this.metas);
      },
    error: (err: Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados das metas',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
    }
  })
}

}
