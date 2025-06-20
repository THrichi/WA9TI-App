import { Component } from '@angular/core';
import {TranslateDirective, TranslatePipe, TranslateService} from '@ngx-translate/core';
import {LoginComponent} from './Login/login/login.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [LoginComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'WA9TI';
  name ="Toumalo";
}
