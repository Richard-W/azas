export interface Participant {
	id: string;
	councilId: string;
	info: any;
	priority: number;
	approved: boolean;
}

export interface Mascot {
	id: string;
	councilId: string;
	fullName: string;
	nickName: string;
}

export interface CouncilInfo {
	id: string;
	token: string;
	university: string;
	address: string;
	email: string;
}

export interface Council {
	info: CouncilInfo;
	participants: Participant[];
	mascots: Mascot[];
}


export interface Field {
	name: string;
	field: string;
	ty: string;
	options: string[];
}

export interface MetaInfo {
	title: string;
	participantType: string;
	types: { [name: string]: Field[] };
	numDisplayedParticipantFields: number;
	allowAdd: boolean;
	allowEdit: boolean;
}

export interface DataDump {
	councils: CouncilInfo[];
	participants: Participant[];
	mascots: Mascot[];
}
