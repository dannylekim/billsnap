package proj.kedabra.billsnap.fixtures;

import proj.kedabra.billsnap.presentation.resources.AnswerNotificationResource;

public final class AnswerNotificationResourceFixture {

    private AnswerNotificationResourceFixture() {}

    public static AnswerNotificationResource getDefault() {
        final AnswerNotificationResource res = new AnswerNotificationResource();
        res.setAnswer(true);
        return res;
    }

}
