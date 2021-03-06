import {Component, DynamicComponentLoader, ElementRef, OnInit, Input, Output, EventEmitter, NgZone} from 'angular2/core'
import {MetaInfo} from './types';

function compileToComponent(template: string) {
}

@Component({
	selector: 'azas-participantform',
	template: '<div #partformcontainer></div>',
})
export class ParticipantFormComponent implements OnInit {
	public static decideFormFieldDisplay(model: any, fieldName: string): boolean {
		return true;
	}

	constructor(private loader: DynamicComponentLoader, private elementRef: ElementRef, private zone: NgZone) {}

	@Input() private meta: MetaInfo;
	@Input() private model: any = null;
	@Input() private submitText: string = "Submit";
	@Output() private submitForm: EventEmitter<any> = new EventEmitter();

	public ngOnInit() {
		var template = "<form>"
		var model: any = {};
		var options = [];
		var nextID = 1;

		function addFormElements(typeName: string, modelPrefix: string, types: any, model: any) {
			var fieldArray = types[typeName];
			for (var key in fieldArray) {
				var field: any = fieldArray[key];
				switch (field.ty) {
				case "Int":
					template += "<div *ngIf=\"decide(model, '"+field.field+"')\">";
					template += "<label>"+field.name+"</label><br />";
					template += "<input type=\"number\" [(ngModel)]=\""+modelPrefix+"."+field.field+"\" /><br />";
					template += "</div>";
					model[field.field] = 0;
					break;
				case "String":
					template += "<div *ngIf=\"decide(model, '"+field.field+"')\">";
					template += "<label>"+field.name+"</label><br />";
					if (field.options) {
						template += "<select [(ngModel)]=\""+modelPrefix+"."+field.field+"\" (change)=\""+modelPrefix+"."+field.field+" = $event.target.selectedOptions[0].label\">";
						template += "<option *ngFor=\"#option of options["+nextID+"]\" [ngValue]=\"option\">{{option}}</option>";
						template += "</select><br />";
						model[field.field] = field.options[0]
						options[nextID] = field.options;
						nextID++;
					} else {
						template += "<input field=\"text\" [(ngModel)]=\""+modelPrefix+"."+field.field+"\" /><br />";
						model[field.field] = '';
					}
					template += "</div>";
					break;
				case "Boolean":
					template += "<div *ngIf=\"decide(model, '"+field.field+"')\">";
					template += "<label>"+field.name+"</label><br />";
					template += "<input field=\"checkbox\" [(ngModel)]=\""+modelPrefix+"."+field.field+"\" /><br />";
					model[field.field] = false;
					template += "</div>";
					break;
				default:
					model[field.field] = {};
					addFormElements(field.ty, modelPrefix + "." + field.field, types, model[field.field]);
					break;
				}
			}
		}
		addFormElements(this.meta.participantType, "model", this.meta.types, model);

		if(this.model != null) {
			/* There already is a model that has to be edited*/
			model = this.model;
		}

		template += "<button (click)=\"onSubmit()\">"+this.submitText+"</button>"
		template += "</form>";

		var submitForm = this.submitForm
		@Component({
			selector: 'dynamic',
			template
		})
		class DynamicComponent {
			public model: any = model;
			public submitForm: EventEmitter<any> = submitForm;
			public options = options;

			private decide(model: any, name: string): boolean {
				return ParticipantFormComponent.decideFormFieldDisplay(model, name);
			}

			private onSubmit() {
				this.submitForm.emit(this.model);
			}
		};

		this.loader.loadIntoLocation(
			DynamicComponent,
			this.elementRef,
			'partformcontainer'
		).then(
		success => {
			this.zone.run(() => {});
		}, error => {
			console.log(error);
			this.zone.run(() => {});
		});
	}
}
