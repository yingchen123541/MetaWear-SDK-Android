/*
 * Copyright 2014-2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */

package com.mbientlab.metawear.impl;

import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.module.MagnetometerBmm150;

import bolts.Task;

import static com.mbientlab.metawear.impl.ModuleId.MAGNETOMETER;

/**
 * Created by etsai on 9/20/16.
 */
class MagnetometerBmm150Impl extends ModuleImplBase implements MagnetometerBmm150 {
    private final static String BFIELD_PRODUCER= "com.mbientlab.metawear.impl.MagnetometerBmm150Impl.BFIELD_PRODUCER",
            BFIELD_X_AXIS_PRODUCER= "com.mbientlab.metawear.impl.MagnetometerBmm150Impl.BFIELD_X_AXIS_PRODUCER",
            BFIELD_Y_AXIS_PRODUCER= "com.mbientlab.metawear.impl.MagnetometerBmm150Impl.BFIELD_Y_AXIS_PRODUCER",
            BFIELD_Z_AXIS_PRODUCER= "com.mbientlab.metawear.impl.MagnetometerBmm150Impl.BFIELD_Z_AXIS_PRODUCER",
            BFIELD_PACKED_PRODUCER= "com.mbientlab.metawear.impl.MagnetometerBmm150Impl.BFIELD_PACKED_PRODUCER";
    private static final byte POWER_MODE = 1,
        DATA_INTERRUPT_ENABLE = 2, DATA_RATE = 3, DATA_REPETITIONS = 4, MAG_DATA = 5,
        PACKED_MAG_DATA = 0x09;
    private static final long serialVersionUID = -8266211541629259291L;

    private static class Bmm150CartesianFloatData extends CartesianFloatData {
        private static final long serialVersionUID = -1411571904651005619L;

        Bmm150CartesianFloatData() {
            this(MAG_DATA, (byte) 1);
        }

        Bmm150CartesianFloatData(byte register, byte copies) {
            super(MAGNETOMETER, register, new DataAttributes(new byte[] {2, 2, 2}, copies, (byte) 0, true));
        }

        Bmm150CartesianFloatData(DataTypeBase input, ModuleId module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        protected float scale(MetaWearBoardPrivate owner) {
            return 16.f;
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, ModuleId module, byte register, byte id, DataAttributes attributes) {
            return new Bmm150CartesianFloatData(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase[] createSplits() {
            return new DataTypeBase[] {new Bmm150SFloatData((byte) 0), new Bmm150SFloatData((byte) 2), new Bmm150SFloatData((byte) 4)};
        }
    }
    private static class Bmm150SFloatData extends SFloatData {
        private static final long serialVersionUID = 3109599023484783057L;

        Bmm150SFloatData(byte offset) {
            super(MAGNETOMETER, MAG_DATA, new DataAttributes(new byte[] {2}, (byte) 1, offset, true));
        }

        Bmm150SFloatData(DataTypeBase input, ModuleId module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        protected float scale(MetaWearBoardPrivate owner) {
            return 16.f;
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, ModuleId module, byte register, byte id, DataAttributes attributes) {
            return new Bmm150SFloatData(input, module, register, id, attributes);
        }
    }

    private transient AsyncDataProducer bfield, packedBfield;

    MagnetometerBmm150Impl(MetaWearBoardPrivate mwPrivate) {
        super(mwPrivate);

        DataTypeBase cfProducer = new Bmm150CartesianFloatData();
        mwPrivate.tagProducer(BFIELD_PRODUCER, cfProducer);
        mwPrivate.tagProducer(BFIELD_X_AXIS_PRODUCER, cfProducer.split[0]);
        mwPrivate.tagProducer(BFIELD_Y_AXIS_PRODUCER, cfProducer.split[1]);
        mwPrivate.tagProducer(BFIELD_Z_AXIS_PRODUCER, cfProducer.split[2]);
        mwPrivate.tagProducer(BFIELD_PACKED_PRODUCER, new Bmm150CartesianFloatData(PACKED_MAG_DATA, (byte) 3));
    }

    @Override
    public MagneticFieldDataProducer magneticField() {
        if (bfield == null) {
            bfield = new MagneticFieldDataProducer() {
                @Override
                public String xAxisName() {
                    return BFIELD_X_AXIS_PRODUCER;
                }

                @Override
                public String yAxisName() {
                    return BFIELD_Y_AXIS_PRODUCER;
                }

                @Override
                public String zAxisName() {
                    return BFIELD_Z_AXIS_PRODUCER;
                }

                @Override
                public Task<Route> addRoute(RouteBuilder builder) {
                    return mwPrivate.queueRouteBuilder(builder, BFIELD_PRODUCER);
                }

                @Override
                public String name() {
                    return BFIELD_PRODUCER;
                }

                @Override
                public void stop() {
                    mwPrivate.sendCommand(new byte[]{MAGNETOMETER.id, DATA_INTERRUPT_ENABLE, 0, 1});
                }

                @Override
                public void start() {
                    mwPrivate.sendCommand(new byte[]{MAGNETOMETER.id, DATA_INTERRUPT_ENABLE, 1, 0});
                }
            };
        }
        return (MagneticFieldDataProducer) bfield;
    }

    @Override
    public AsyncDataProducer packedMagneticField() {
        if (packedBfield == null) {
            packedBfield = new AsyncDataProducer() {
                @Override
                public Task<Route> addRoute(RouteBuilder builder) {
                    return mwPrivate.queueRouteBuilder(builder, BFIELD_PACKED_PRODUCER);
                }

                @Override
                public String name() {
                    return BFIELD_PACKED_PRODUCER;
                }

                @Override
                public void stop() {
                    mwPrivate.sendCommand(new byte[]{MAGNETOMETER.id, DATA_INTERRUPT_ENABLE, 0, 1});
                }

                @Override
                public void start() {
                    mwPrivate.sendCommand(new byte[]{MAGNETOMETER.id, DATA_INTERRUPT_ENABLE, 1, 0});
                }
            };
        }
        return packedBfield;
    }

    @Override
    public ConfigEditor configure() {
        return new ConfigEditor() {
            private short xyReps = 9, zReps = 15;
            private OutputDataRate odr = OutputDataRate.ODR_10_HZ;

            @Override
            public ConfigEditor xyReps(short reps) {
                xyReps = reps;
                return this;
            }

            @Override
            public ConfigEditor zReps(short reps) {
                zReps = reps;
                return this;
            }

            @Override
            public ConfigEditor outputDataRate(OutputDataRate odr) {
                this.odr = odr;
                return this;
            }

            @Override
            public void commit() {
                mwPrivate.sendCommand(new byte[] {MAGNETOMETER.id, DATA_REPETITIONS, (byte) ((xyReps - 1) / 2), (byte) (zReps - 1)});
                mwPrivate.sendCommand(new byte[] {MAGNETOMETER.id, DATA_RATE, (byte) odr.ordinal()});
            }
        };
    }

    @Override
    public void usePreset(Preset preset) {
        switch (preset) {
            case LOW_POWER:
                configure()
                        .xyReps((short) 3)
                        .zReps((short) 3)
                        .outputDataRate(OutputDataRate.ODR_10_HZ)
                        .commit();
                break;
            case REGULAR:
                configure()
                        .xyReps((short) 9)
                        .zReps((short) 15)
                        .outputDataRate(OutputDataRate.ODR_10_HZ)
                        .commit();
                break;
            case ENHANCED_REGULAR:
                configure()
                        .xyReps((short) 15)
                        .zReps((short) 27)
                        .outputDataRate(OutputDataRate.ODR_10_HZ)
                        .commit();
                break;
            case HIGH_ACCURACY:
                configure()
                        .xyReps((short) 47)
                        .zReps((short) 83)
                        .outputDataRate(OutputDataRate.ODR_20_HZ)
                        .commit();
                break;
        }
    }


    @Override
    public void start() {
        mwPrivate.sendCommand(new byte[] {MAGNETOMETER.id, POWER_MODE, 1});
    }

    @Override
    public void stop() {
        mwPrivate.sendCommand(new byte[] {MAGNETOMETER.id, POWER_MODE, 0});
    }
}
