import {Component, Output, EventEmitter} from 'angular2/core';

@Component({
	selector: 'azas-masterpass',
	template:`
	<form>
	<label for="token">Master password:</label><br />
	<input type="text" [(ngModel)]="pass" />
	<button (click)="onPasswordSubmit()">Abschicken</button>
	</form>
	`
})
export class PasswordComponent {
	private pass: string = '';

	@Output() password: EventEmitter<string> = new EventEmitter<string>();

	private onPasswordSubmit() {
		this.password.emit(this.pass);
	}
}
