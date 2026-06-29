import { Column, Entity, Index, JoinColumn, ManyToOne, PrimaryGeneratedColumn } from "typeorm";
import { Room } from "./room.entity";

@Entity()
@Index(["room"])
export class Stroke {
    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Column('json')
    points: { x: number; y: number, color: number, strokeWidth: number }[];

    @Column()
    color: number;

    @Column('float')
    strokeWidth: number;

    @ManyToOne(() => Room, { onDelete: 'CASCADE' }) 
    @JoinColumn({ name: 'roomId' })
    room: Room;
}