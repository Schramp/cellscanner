package nl.nfi.cellscanner.collect.phonestate;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import nl.nfi.cellscanner.collect.SubscriptionDataCollector;
import nl.nfi.cellscanner.collect.RecordingService;

/**
 * The API used by this class is deprecated in android API level 31
 */
@Deprecated
public class PhoneStateDataCollector extends SubscriptionDataCollector {
    private Map<String, PhoneStateCallback> cellinfo_callbacks = new HashMap<>();
    private Map<String, PhoneStateCallback> callstate_callbacks = new HashMap<>();

    /**
     * The API used by this class is deprecated in android API level 31
     */
    public PhoneStateDataCollector(RecordingService service) {
        super(service);
    }

    public void update(Context ctx, Intent intent, boolean enable) {
        CallbackFactory cellinfo_factory = new PhoneStateFactory.CellInfoFactory();
        CallbackFactory callstate_factory = new PhoneStateFactory.CallStateFactory();

        cellinfo_callbacks = updateCallbacks(ctx, intent, cellinfo_callbacks, cellinfo_factory, enable);
        callstate_callbacks = updateCallbacks(ctx, intent, callstate_callbacks, callstate_factory, enable);
    }
}