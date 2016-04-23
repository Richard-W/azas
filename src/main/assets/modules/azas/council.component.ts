import {Component, Input, OnInit, NgZone} from 'angular2/core';
import {AzasService} from './azas.service';
import {ParticipantFormComponent} from './participantform.component';
import {DisplayParticipantsComponent} from './displayparticipants.component';
import {MascotFormComponent} from './mascotform.component';
import {DisplayMascotsComponent} from './displaymascots.component';
import {Council, MetaInfo, Participant, Mascot} from './types';

@Component({
	selector: 'azas-council',
	directives: [
		DisplayParticipantsComponent,
		ParticipantFormComponent,
		DisplayMascotsComponent,
		MascotFormComponent
	],
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
		<azas-displayparticipants [meta]="meta" [actions]="[{id: 0, name: 'Ändern'}, {id: 1, name: 'Löschen'}]" [participants]="council.participants" (action)="onParticipantsAction($event)"></azas-displayparticipants>
		<div *ngIf="displayAddParticipant">
			<azas-participantform [meta]="meta" (submitForm)="onSubmitAddParticipant($event)" [submitText]="'Eintragen'"></azas-participantform>
			<button (click)="abortAdd()">Abbrechen</button>
		</div>
		<div *ngIf="editeeParticipant != null"> 
			<azas-participantform *ngIf="editeeParticipant != null" [meta]="meta" (submitForm)="onSubmitEditParticipant($event)" [model]="editeeParticipantInfo" [submitText]="'Ändern'"></azas-participantform>
			<button (click)="abortEdit()">Abbrechen</button>
		</div>
		<button *ngIf="!displayAddParticipant" (click)="addParticipant()">Teilnehmer hinzufügen</button>
		<h3>Maskottchen</h3>
		<azas-displaymascots [mascots]="council.mascots" [actions]="[{id: 0, name: 'Ändern'}, {id: 1, name: 'Löschen'}]" (action)="onMascotsAction($event)"></azas-displaymascots>
		<div *ngIf="displayAddMascot">
			<azas-mascotform [submitText]="'Eintragen'" (submitForm)="onSubmitAddMascot($event)"></azas-mascotform>
			<button (click)="abortAddMascot()">Abbrechen</button>
		</div>
		<div *ngIf="editeeMascot != null">
			<azas-mascotform [model]="editeeMascot" [submitText]="'Ändern'" (submitForm)="onSubmitEditMascot($event)"></azas-mascotform>
			<button (click)="abortEditMascot()">Abbrechen</button>
		</div>
		<button *ngIf="!displayAddMascot" (click)="addMascot()">Maskottchen hinzufügen</button>
	</div>
	`
})
export class CouncilComponent implements OnInit {
	@Input() token: string;
	@Input() meta: MetaInfo;

	private council: Council = null;
	private error: string = '';

	constructor(private azas: AzasService, private zone: NgZone) {}

	public ngOnInit() {
		this.reloadCouncil();
	}

	private onParticipantsAction(action: {id: number, target: Participant}) {
		switch (action.id) {
		case 0:
			this.editParticipant(action.target);
			break;
		case 1:
			this.deleteParticipant(action.target);
			break;
		}
	}

	private onMascotsAction(action: {id: number, target: Mascot}) {
		switch (action.id) {
		case 0:
			this.editMascot(action.target);
			break;
		case 1:
			this.deleteMascot(action.target);
			break;
		}
	}

	private deleteParticipant(participant: Participant) {
		this.azas.deleteParticipant(this.token, participant).subscribe(
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

	private abortAdd() {
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
		this.editeeParticipant.info = event;
		this.azas.editParticipant(this.token, this.editeeParticipant).subscribe(
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

	/* Add mascot */

	private displayAddMascot: boolean = false;
	
	private addMascot() {
		this.displayAddMascot = true;
		setTimeout(() => { this.editeeMascot = null; });
	}

	private abortAddMascot() {
		setTimeout(() => { this.displayAddMascot = false; });
	}
	
	private onSubmitAddMascot(mascot: Mascot) {
		this.azas.addMascot(this.token, mascot.fullName, mascot.nickName).subscribe(
			success => {
				this.reloadCouncil();
			}, error => {
				this.error = JSON.stringify(error, null, 2);
			}
		);
		setTimeout(() => { this.displayAddMascot = false });
	}

	/* Edit mascot */

	private editeeMascot: Mascot = null;

	private editMascot(mascot: Mascot) {
		this.editeeMascot = mascot;
		setTimeout(() => { this.displayAddMascot = false });
	}

	private abortEditMascot() {
		this.editeeMascot = null;
	}

	private onSubmitEditMascot(mascot: Mascot) {
		this.azas.editMascot(this.token, mascot).subscribe(
			success => {
				this.reloadCouncil();
			}, error => {
				this.error = JSON.stringify(error, null, 2);
			}
		);
		setTimeout(() => { this.editeeMascot = null });
	}

	/* Delete mascot */
	
	private deleteMascot(mascot: Mascot) {
		this.azas.deleteMascot(this.token, mascot).subscribe(
			success => {
				this.reloadCouncil();
			}, error => {
				this.error = JSON.stringify(error, null, 2);
			}
		);
	}
}
