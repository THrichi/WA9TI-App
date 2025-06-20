import { Component } from '@angular/core';
import {TranslateDirective, TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  imports: [TranslatePipe, TranslateDirective],
  templateUrl: './login.component.html',
  standalone: true,
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  title = 'WA9TI';
  name ="Toumalo";

  constructor(private translate: TranslateService) {
    this.translate.addLangs(['fr', 'en', 'ar']);
    this.translate.setDefaultLang('fr');
    this.translate.use('fr');
  }

  changeLanguage(lang: string) {
    this.translate.use(lang);
  }

}
