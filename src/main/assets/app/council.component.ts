import {Component, Input, OnInit} from 'angular2/core';
import {AzasService} from './azas.service';

@Component({
    selector: 'azas-council',
    template:`
        <p>Council token: {{token}}</p>
        <div *ngIf="council == ''">
            <p>Loading...</p>
        </div>
        <pre>
            {{council}}
        </pre>
    `
})
export class CouncilComponent implements OnInit {
    @Input() token: string;

    private council: string = '';

    constructor(private azas: AzasService) {}

    public ngOnInit() {
        this.azas.getCouncil(this.token).subscribe(
            council => {
                this.council = JSON.stringify(council);
            },
            error => {
                alert(error.status);
            }
        );
    }
}