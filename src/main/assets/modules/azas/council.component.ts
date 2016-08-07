import {Component, Input, OnInit, NgZone} from 'angular2/core';
import {AzasService} from './azas.service';
import {ParticipantFormComponent} from './participantform.component';
import {DisplayParticipantsComponent} from './displayparticipants.component';
import {MascotFormComponent} from './mascotform.component';
import {DisplayMascotsComponent} from './displaymascots.component';
import {Council, MetaInfo, Participant, Mascot} from './types';
import {Observable} from 'rxjs/Rx';

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
		<div id="azasDeletionConfirmation" *ngIf="deletee != null">
		<p>Wirklich löschen?</p>
		<button (click)="deleteParticipant(deletee)">Ja</button>
		<button (click)="deletee = null">Nein</button>
		</div>
		<azas-displayparticipants [meta]="meta" [actions]="actions" [participants]="council.participants" (action)="onParticipantsAction($event)"></azas-displayparticipants>
		<div *ngIf="displayAddParticipant">
			<azas-participantform [meta]="meta" (submitForm)="onSubmitAddParticipant($event)" [submitText]="'Eintragen'"></azas-participantform>
			<button (click)="abortAdd()">Abbrechen</button>
		</div>
		<div *ngIf="editeeParticipant != null"> 
			<azas-participantform *ngIf="editeeParticipant != null" [meta]="meta" (submitForm)="onSubmitEditParticipant($event)" [model]="editeeParticipant.info" [submitText]="'Ändern'"></azas-participantform>
			<button (click)="abortEdit()">Abbrechen</button>
		</div>
		<button *ngIf="!displayAddParticipant && meta.allowAdd" (click)="addParticipant()">Teilnehmer hinzufügen</button>
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
	public static consumeCouncil(council: Council): void {}

	@Input() token: string;
	@Input() meta: MetaInfo;

	private council: Council = null;
	private error: string = '';
	private actions: any[] = [];
	private deletee: Participant = null;

	constructor(private azas: AzasService, private zone: NgZone) {}

	public ngOnInit() {
		this.reloadCouncil();
		if (this.meta.allowEdit) {
			this.actions.push({id: 0, name: 'Ändern'});
		}
		if (this.meta.allowAdd) {
			this.actions.push({id: 1, name: 'Löschen'});
		}
		if (this.meta.allowEdit) {
			this.actions.push({id: 2, name: '\u{2191}'});
			this.actions.push({id: 3, name: '\u{2193}'});
		}
	}

	private onParticipantsAction(action: {id: number, target: Participant}) {
		switch (action.id) {
		case 0:
			this.editParticipant(action.target);
			break;
		case 1:
			this.deletee = action.target;
			break;
		case 2:
			if(this.council.participants.length > 1) {
				var index = this.council.participants.indexOf(action.target);
				if (index > 0) {
					this.swapPriority(index, index - 1);
				}
			}
			break;
		case 3:
			if(this.council.participants.length > 1) {
				var index = this.council.participants.indexOf(action.target);
				if (index < (this.council.participants.length - 1)) {
					this.swapPriority(index, index + 1);
				}
			}
			break;
		}
	}

	private swapPriority(i: number, j: number) {
		var p1 = this.council.participants[i];
		var p2 = this.council.participants[j];
		var swap = p1.priority;
		p1.priority = p2.priority;
		p2.priority = swap;
		var obs1 = this.azas.editParticipant(this.token, p1);
		var obs2 = this.azas.editParticipant(this.token, p2);
		var obs = Observable.merge(obs1, obs2);
		obs.subscribe(
			null,
			error => {
				this.error = JSON.stringify(error, null, 2);
			}, () => {
				this.reloadCouncil();
			}
		);
	}

	private validatePriorities() {
		var observable: Observable<void> = null;

		for(var keyString in this.council.participants) {
			var key: number = parseInt(keyString);
			if(key != this.council.participants[key].priority) {
				this.council.participants[key].priority = key;
				var edit = this.azas.editParticipant(this.token, this.council.participants[key]);
				if (observable == null) observable = edit;
				else observable = Observable.merge(observable, edit);
			}
		}
		if(observable != null) {
			observable.subscribe(
				null,
				error => {
					this.error = JSON.stringify(error, null, 2);
				}, () => {
					this.reloadCouncil();
				}
			);
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
		this.deletee = null;
	}

	/* Display participants */

	private reloadCouncil() {
		this.azas.getCouncil(this.token).subscribe(
			council => {
				this.council = council;
				this.council.participants.sort((p1, p2) => p1.priority - p2.priority);
				this.validatePriorities();
				CouncilComponent.consumeCouncil(this.council);
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
	}

	private onSubmitAddParticipant(event: any) {
		var priority: number
		if(this.council.participants.length == 0) {
			priority = 0;
		} else {
			priority = this.council.participants[this.council.participants.length - 1].priority + 1;
		}
		this.azas.addParticipant(this.token, event, priority).subscribe(
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

	private editeeParticipant: Participant = null;

	private editParticipant(participant: Participant) {
		this.editeeParticipant = JSON.parse(JSON.stringify(participant));
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
	}

	private abortEdit() {
		setTimeout(() => { this.editeeParticipant = null; }, 0);
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
		this.editeeMascot = JSON.parse(JSON.stringify(mascot));
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
