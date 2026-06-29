import { IsArray, IsNumber, IsUUID, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';


class PointDTO {
    @IsNumber()
    x: number;

    @IsNumber()
    y: number;

    @IsNumber()
    color: number;

    @IsNumber()
    strokeWidth: number;
}

export class SaveStrokeDTO {
    @IsArray()
    @ValidateNested({ each: true })
    @Type(() => PointDTO)
    points: PointDTO[];

    @IsNumber()
    color: number;

    @IsNumber()
    strokeWidth: number;
}