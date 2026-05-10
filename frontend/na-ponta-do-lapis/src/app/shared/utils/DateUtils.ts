import da from "@angular/common/locales/da";

export class DateUtils {

    static formatarParaEnvioGlobal(data:Date):string {
        return data.toISOString();
    }
}