import {Component, Input, Output, EventEmitter} from 'angular2/core';
import {Mascot} from './types';

@Component({
	selector: 'azas-mascotform',
	template:`
	<form>
		<label>Voller Name</label><br />
		<input type="text" [(ngModel)]="model.fullName" /><br />
		<label>Spitzname</label><br />
		<input type="text" [(ngModel)]="model.nickName" /><br />
		<button (click)="onSubmitForm()">{{submitText}}</button>
	</form>
	`
})
export class MascotFormComponent {
	@Input() model: Mascot = {id: '', councilId: '', fullName: '', nickName: ''};
	@Input() submitText: string = "Abschicken";

	@Output() submitForm: EventEmitter<Mascot> = new EventEmitter<Mascot>();

	private onSubmitForm() {
		this.submitForm.emit(this.model);
	}
}
