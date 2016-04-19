import {Component, DynamicComponentLoader, ElementRef, OnInit, Input, Output, EventEmitter, NgZone} from 'angular2/core'

function compileToComponent(template: string) {
}

@Component({
    selector: 'participant-form',
    template: '<div #partformcontainer></div>',
})
export class ParticipantFormComponent implements OnInit {
    constructor(private loader: DynamicComponentLoader, private elementRef: ElementRef, private zone: NgZone) {}

    @Input() private meta: any;
    @Output() private submitForm: EventEmitter<any> = new EventEmitter();

    public ngOnInit() {
        var template = "<form>"
        var form: any = {};
        var options = [];
        var nextID = 1;

        function addFormElements(typeName: string, modelPrefix: string, types: any, form: any) {
            var fieldArray = types[typeName];
            for (var key in fieldArray) {
                var field: any = fieldArray[key];
                switch (field.ty) {
                    case "Int":
                        template += "<label>"+field.name+"</label><br />";
                        template += "<input field=\"number\" #"+field.field+" (change)=\""+modelPrefix+"."+field.field+" = parseInt("+field.field+".value)\"/><br />";
                        form[field.field] = 0;
                        break;
                    case "String":
                        template += "<label>"+field.name+"</label><br />";
                        if (field.options) {
                            template += "<select #id"+nextID+" [ngModel]=\""+modelPrefix+"."+field.field+"\" (change)=\""+modelPrefix+"."+field.field+" = id"+nextID+".value\">";
                            template += "<option *ngFor=\"#option of options["+nextID+"]\" [value]=\"option\">{{option}}</option>";
                            template += "</select><br />";
                            form[field.field] = field.options[0]
                            options[nextID] = field.options;
                            nextID++;
                        } else {
                            template += "<input field=\"text\" [(ngModel)]=\""+modelPrefix+"."+field.field+"\" /><br />";
                            form[field.field] = '';
                        }
                        break;
                    case "Boolean":
                        template += "<label>"+field.name+"</label><br />";
                        template += "<input field=\"checkbox\" [(ngModel)]=\""+modelPrefix+"."+field.field+"\" /><br />";
                        form[field.field] = false;
                        break;
                    default:
                        form[field.field] = {};
                        addFormElements(field.ty, modelPrefix + "." + field.field, types, form[field.field]);
                }
            }
        }
        addFormElements(this.meta.participantType, "form", this.meta.types, form);

        template += "<button (click)=\"onSubmit()\">Eintragen</button>"
        template += "</form>";

        var submitForm = this.submitForm
        @Component({
            selector: 'dynamic',
            template
        })
        class DynamicComponent {
            public form: any = form;
            public submitForm: EventEmitter<any> = submitForm;
            public options = options;

            private stringify(obj: any) {
                return JSON.stringify(obj, null, 2);
            }

            private parseInt(str: string): number {
                return parseInt(str);
            }

            private onSubmit() {
                this.submitForm.emit(this.form);
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
            }
        );
  }
}