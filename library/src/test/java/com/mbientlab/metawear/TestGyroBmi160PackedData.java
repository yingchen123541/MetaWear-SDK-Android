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

import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.data.AngularVelocity;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by etsai on 11/17/16.
 */
public class TestGyroBmi160PackedData extends UnitTestBase {
    private GyroBmi160 gyroBmi160;

    @Before
    public void setup() throws Exception {
        junitPlatform.boardInfo = new MetaWearBoardInfo(GyroBmi160.class);
        connectToBoard();

        gyroBmi160= mwBoard.getModule(GyroBmi160.class);
        gyroBmi160.packedAngularVelocity().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        ((ArrayList<AngularVelocity>) env[0]).add(data.value(AngularVelocity.class));
                    }
                });
            }
        });
    }

    @Test
    public void interpretPackedData() {
        byte[] response = new byte[] {0x13, 0x07, 0x09, 0x15, (byte) 0xad, 0x26, 0x08, (byte) 0xde, (byte) 0x8a, 0x1a, 0x0d,
                0x26, 0x65, (byte) 0xe4, (byte) 0x8d, 0x20, (byte) 0xac, 0x27, 0x73, (byte) 0xec};
        AngularVelocity[] expected = new AngularVelocity[] {
                new AngularVelocity(Float.intBitsToFloat(0x43242d45), Float.intBitsToFloat(0x4396ee0d), Float.intBitsToFloat(0xc3848f9c)),
                new AngularVelocity(Float.intBitsToFloat(0x434f2258), Float.intBitsToFloat(0x43947da9), Float.intBitsToFloat(0xc3577513)),
                new AngularVelocity(Float.intBitsToFloat(0x437e0e0d), Float.intBitsToFloat(0x439ad12c), Float.intBitsToFloat(0xc318976a))
        };

        gyroBmi160.configure()
                .range(GyroBmi160.Range.FSR_1000)
                .commit();

        final ArrayList<AngularVelocity> received = new ArrayList<>();
        mwBoard.lookupRoute(0).setEnvironment(0, received);

        sendMockResponse(response);
        AngularVelocity[] actual = new AngularVelocity[3];
        received.toArray(actual);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void subscribe() {
        byte[] expected = new byte[] {0x13, 0x07, 0x01};
        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }

    @Test
    public void unsubscribe() {
        byte[] expected = new byte[] {0x13, 0x07, 0x00};
        mwBoard.lookupRoute(0).unsubscribe(0);
        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }

    @Test
    public void enable() {
        byte[] expected = new byte[] {0x13, 0x02, 0x00, 0x01};

        gyroBmi160.packedAngularVelocity().stop();
        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }

    @Test
    public void disable() {
        byte[] expected = new byte[] {0x13, 0x02, 0x01, 0x00};

        gyroBmi160.packedAngularVelocity().start();
        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }
}
