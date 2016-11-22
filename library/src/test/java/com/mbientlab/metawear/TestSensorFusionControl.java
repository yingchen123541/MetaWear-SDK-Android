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

package com.mbientlab.metawear;

import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.SensorFusion;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.mbientlab.metawear.TestGyroBmi160Config.ODR_BITMASK;
import static com.mbientlab.metawear.TestGyroBmi160Config.RANGE_BITMASK;
import static com.mbientlab.metawear.TestSensorFusionConfig.BMI160_ACC_RANGE_BITMASK;
import static com.mbientlab.metawear.module.SensorFusion.Mode.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by etsai on 11/12/16.
 */
@RunWith(Parameterized.class)
public class TestSensorFusionControl extends UnitTestBase {
    @Parameters(name = "{0}, {2}")
    public static Collection<Object[]> data() {
        String[] functionNames = new String[] {
            "correctedAcceleration",
            "correctedRotation",
            "correctedBField",
            "quaternion",
            "eulerAngles",
            "gravity",
            "linearAcceleration"
        };

        Object[][] testBases = new Object[][]{
            {
                NDOF,
                new byte[][] {
                    {0x19, 0x02, 0x01, 0x13},
                    {0x03, 0x03, 0x28, 0x0c},
                    {0x13, 0x03, 0x28, 0x00},
                    {0x15, 0x04, 0x04, 0x0e},
                    {0x15, 0x03, 0x6},
                    {0x03, 0x02, 0x01, 0x00},
                    {0x13, 0x02, 0x01, 0x00},
                    {0x15, 0x02, 0x01, 0x00},
                    {0x03, 0x01, 0x01},
                    {0x13, 0x01, 0x01},
                    {0x15, 0x01, 0x01},
                    {0x19, 0x03, 0x00, 0x00},
                    {0x19, 0x01, 0x01},
                    {0x19, 0x01, 0x00},
                    {0x19, 0x03, 0x00, 0x7f},
                    {0x03, 0x01, 0x00},
                    {0x13, 0x01, 0x00},
                    {0x15, 0x01, 0x00},
                    {0x03, 0x02, 0x00, 0x01},
                    {0x13, 0x02, 0x00, 0x01},
                    {0x15, 0x02, 0x00, 0x01}
                },
                11
            },
            {
                IMU_PLUS,
                new byte[][] {
                    {0x19, 0x02, 0x02, 0x13},
                    {0x03, 0x03, 0x28, 0x0c},
                    {0x13, 0x03, 0x28, 0x00},
                    {0x03, 0x02, 0x01, 0x00},
                    {0x13, 0x02, 0x01, 0x00},
                    {0x03, 0x01, 0x01},
                    {0x13, 0x01, 0x01},
                    {0x19, 0x03, 0x00, 0x00},
                    {0x19, 0x01, 0x01},
                    {0x19, 0x01, 0x00},
                    {0x19, 0x03, 0x00, 0x7f},
                    {0x03, 0x01, 0x00},
                    {0x13, 0x01, 0x00},
                    {0x03, 0x02, 0x00, 0x01},
                    {0x13, 0x02, 0x00, 0x01}
                },
                7
            },
            {
                COMPASS,
                new byte[][] {
                    {0x19, 0x02, 0x03, 0x13},
                    {0x03, 0x03, 0x26, 0x0c},
                    {0x15, 0x04, 0x04, 0x0e},
                    {0x15, 0x03, 0x6},
                    {0x03, 0x02, 0x01, 0x00},
                    {0x15, 0x02, 0x01, 0x00},
                    {0x03, 0x01, 0x01},
                    {0x15, 0x01, 0x01},
                    {0x19, 0x03, 0x00, 0x00},
                    {0x19, 0x01, 0x01},
                    {0x19, 0x01, 0x00},
                    {0x19, 0x03, 0x00, 0x7f},
                    {0x03, 0x01, 0x00},
                    {0x15, 0x01, 0x00},
                    {0x03, 0x02, 0x00, 0x01},
                    {0x15, 0x02, 0x00, 0x01}
                },
                8
            },
            {
                M4G,
                new byte[][] {
                    {0x19, 0x02, 0x04, 0x13},
                    {0x03, 0x03, 0x27, 0x0c},
                    {0x15, 0x04, 0x04, 0x0e},
                    {0x15, 0x03, 0x6},
                    {0x03, 0x02, 0x01, 0x00},
                    {0x15, 0x02, 0x01, 0x00},
                    {0x03, 0x01, 0x01},
                    {0x15, 0x01, 0x01},
                    {0x19, 0x03, 0x00, 0x00},
                    {0x19, 0x01, 0x01},
                    {0x19, 0x01, 0x00},
                    {0x19, 0x03, 0x00, 0x7f},
                    {0x03, 0x01, 0x00},
                    {0x15, 0x01, 0x00},
                    {0x03, 0x02, 0x00, 0x01},
                    {0x15, 0x02, 0x00, 0x01}
                },
                8
            }
        };

        ArrayList<Object[]> tests = new ArrayList<>();
        for(int i= 0; i < 7; i++) {
            for(Object[] base: testBases) {
                Object[] config = new Object[base.length];
                config[0] = base[0];
                config[2] = functionNames[i];

                byte[][] copy = new byte[((byte[][]) base[1]).length][];
                for(int j = 0; j < copy.length; j++) {
                    copy[j] = Arrays.copyOf(((byte[][]) base[1])[j], ((byte[][]) base[1])[j].length);
                }
                config[1] = copy;

                ((byte[][]) config[1])[(int) base[2]][2] |= (0x1 << i);
                tests.add(config);
            }
        }

        return tests;
    }

    @Parameter
    public SensorFusion.Mode opMode;

    @Parameter(value = 1)
    public byte[][] expected;

    @Parameter(value = 2)
    public String fnName;

    private SensorFusion sensorFusion;

    @Before
    public void setup() throws Exception {
        btlePlaform.boardInfo = MetaWearBoardInfo.MOTIOON_R;
        connectToBoard();

        sensorFusion = mwBoard.getModule(SensorFusion.class);
    }

    @Test
    public void startAndStop() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = SensorFusion.class.getMethod(fnName);
        AsyncDataProducer producer = (AsyncDataProducer) m.invoke(sensorFusion);

        sensorFusion.configure()
                .mode(opMode)
                .commit();
        producer.start();
        sensorFusion.start();
        sensorFusion.stop();
        producer.stop();

        assertArrayEquals(expected, btlePlaform.getCommands());
    }
}
