import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity()
export class Room {
    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Column({ unique: true })
    inviteCode: string;

    @Column()
    ownerId: string;

    @Column({ nullable: true })
    partnerId: string;
}