import {Component, Input, OnInit, NgZone} from 'angular2/core';
import {AzasService} from './azas.service';
import {ParticipantFormComponent} from './participantform.component'

@Component({
	selector: 'azas-council',
	directives: [ParticipantFormComponent],
	template:`
	<div *ngIf="council == null && error == ''">
		<p>Loading...</p>
	</div>
	<div *ngIf="error != ''">
		<pre>{{error}}</pre>
	</div>
	<div *ngIf="council != null">
		<h2>{{council.info.university}}</h2>
		<h3>Teilnehmer</h3>
		<table>
			<thead>
				<tr>
					<th *ngFor="#name of participantFieldNames()">{{name}}</th>
					<th></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<tr *ngFor="#participant of council.participants">
					<td *ngFor="#field of participantFields(participant)">{{field}}</td>
					<td class="azas-buttoncell"><button (click)="editParticipant(participant)">Ändern</button></td>
					<td class="azas-buttoncell"><button (click)="deleteParticipant(participant.id)">Löschen</button></td>
				</tr>
			</tbody>
		</table>
		<div *ngIf="displayAddParticipant">
			<participant-form [meta]="meta" (submitForm)="onSubmitAddParticipant($event)" [submitText]="'Eintragen'"></participant-form>
		</div>
		<div *ngIf="editeeParticipant != null"> 
			<participant-form *ngIf="editeeParticipant != null" [meta]="meta" (submitForm)="onSubmitEditParticipant($event)" [model]="editeeParticipantInfo" [submitText]="'Ändern'"></participant-form>
			<button (click)="abortEdit()">Abbrechen</button>
		</div>
		<button *ngIf="!displayAddParticipant" (click)="addParticipant()">Teilnehmer hinzufügen</button>
		<h3>Maskottchen</h3>
	</div>
	`
})
export class CouncilComponent implements OnInit {
	@Input() token: string;
	@Input() meta: any;

	private council: any = null;
	private error: string = '';
	private types: string[] = null
	private form: any = {};

	constructor(private azas: AzasService, private zone: NgZone) {}

	public ngOnInit() {
		this.reloadCouncil();
	}

	private keys(obj: Object): string[] {
		return Object.keys(obj);
	}

	private typeFields(field: string): any[] {
		return this.meta.types[field];
	}

		private deleteParticipant(id: string) {
		this.azas.deleteParticipant(this.token, id).subscribe(
			success => {
				this.reloadCouncil();
			},
			error => {
				this.error = JSON.stringify(error, null, 2);
			}
		);
	}

	/* Display participants */

	private reloadCouncil() {
		this.azas.getCouncil(this.token).subscribe(
			council => {
				this.council = council;
				this.zone.run(() => {});
			},
			error => {
				this.error = JSON.stringify(error, null, 2);
				this.zone.run(() => {});
			}
		);
	}

	private participantFieldNames() {
		var fields: string[] = [];
		for (var i = 0; i < this.meta.numDisplayedParticipantFields; ++i) {
			var field = this.meta.types[this.meta.participantType][i];
			fields.push(field.name);
		}
		return fields;
	}

	private participantFields(participant: any) {
		var fields: string[] = [];
		for (var i = 0; i < this.meta.numDisplayedParticipantFields; ++i) {
			var field = this.meta.types[this.meta.participantType][i];
			fields.push(participant.info[field.field]);
		}
		return fields;
	}


	/* Add participant */

	private displayAddParticipant: boolean = false;

	private addParticipant(id: string) {
		this.displayAddParticipant = true;
		this.editeeParticipant = null;
		this.editeeParticipantInfo = null;
	}

	private onSubmitAddParticipant(event: any) {
		this.azas.addParticipant(this.token, event, 0).subscribe(
			success => {
				this.displayAddParticipant = false;
				this.reloadCouncil();
			}, error => {
				this.displayAddParticipant = false;
				this.error = JSON.stringify(error, null, 2);
			}
		);
		setTimeout(() => { this.displayAddParticipant = false; }, 0);
	}

	/* Edit participant */

	private editeeParticipant: any = null;
	private editeeParticipantInfo: any = null;

	private editParticipant(participant: any) {
		this.editeeParticipant = participant;
		this.editeeParticipantInfo = participant.info;
		setTimeout(() => { this.displayAddParticipant = false; }, 0);
	}

	private onSubmitEditParticipant(event: any) {
		this.azas.editParticipant(this.token, this.editeeParticipant.id, event, this.editeeParticipant.priority).subscribe(
			success => {
				this.reloadCouncil();
			}, error => {
				this.error = JSON.stringify(error, null, 2);
			}
		);
		setTimeout(() => { this.editeeParticipant = null; }, 0);
		this.editeeParticipantInfo = null;
	}

	private abortEdit() {
		setTimeout(() => { this.editeeParticipant = null; }, 0);
		this.editeeParticipantInfo = null;
	}
}
