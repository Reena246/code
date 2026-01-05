public AccessResponse validate(AccessRequest request) {

    if (request == null ||
        request.getCardId() == null ||
        request.getDoorId() == null) {

        return new AccessResponse(false, "Invalid request", 0);
    }

    long start = System.currentTimeMillis();

    boolean accessGranted =
        cardAccessRepository.existsByCardIdAndDoorIdAndIsActiveTrue(
            request.getCardId(),
            request.getDoorId()
        );

    long end = System.currentTimeMillis();

    AccessLog log = new AccessLog();
    log.setCardId(request.getCardId());
    log.setDoorId(request.getDoorId());
    log.setLocationId(request.getLocationId());
    log.setAccessGranted(accessGranted);
    log.setRequestTime(LocalDateTime.now());
    log.setResponseTime(LocalDateTime.now());

    accessLogRepository.save(log);

    return new AccessResponse(
        accessGranted,
        accessGranted ? "Access granted" : "Access denied",
        end - start
    );
}
