package com.found_404.funco.follow.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.found_404.funco.follow.exception.FollowException;
import org.hibernate.annotations.Comment;

import com.found_404.funco.global.entity.BaseEntity;
import com.found_404.funco.global.util.CommissionUtil;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.found_404.funco.follow.exception.FollowErrorCode.INSUFFICIENT_ASSET;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

	@Comment("member seq")
	@Column(nullable = false)
	private Long followingMemberId;

	@Comment("member seq")
	@Column(nullable = false)
	private Long followerMemberId;

	@OneToMany(mappedBy = "follow", cascade = CascadeType.ALL)
	private List<FollowingCoin> followingCoins = new ArrayList<>();

	@Comment("초기투자금")
	@Column(nullable = false)
	private Long investment;

	@Comment("가용 현금")
	@Column(nullable = false)
	private Long cash;

	@Comment("수수료")
	private Long commission;

	@Comment("수익률")
	private Double returnRate;

	@Comment("정산날짜")
	private LocalDateTime settleDate;

	@Comment("정산여부")
	private Boolean settled;

	@Comment("정산금액")
	private Long settlement;

	@Builder
	public Follow(Long followingMemberId, Long followerMemberId, Long investment,
		Long cash, Long commission, Double returnRate, LocalDateTime settleDate, Boolean settled, Long settlement) {
		this.followingMemberId = followingMemberId;
		this.followerMemberId = followerMemberId;
		this.investment = investment;
		this.cash = cash;
		this.commission = commission;
		this.returnRate = returnRate;
		this.settleDate = settleDate;
		this.settled = settled;
		this.settlement = settlement;
	}

	public void settleFollow(Long commission, Double returnRate, Long settlement) {
		this.commission = commission;
		this.returnRate = returnRate;
		this.settlement = settlement;
		this.settleDate = LocalDateTime.now();
		this.settled = true;
	}

	public void decreaseCash(long orderCash) {
		if (this.cash < orderCash) {
			throw new FollowException(INSUFFICIENT_ASSET);  // member domain에서 custom exception 추가
		}
		this.cash -= orderCash;
	}

	public void updateCash(long orderCash) {
		if (this.cash + orderCash < 0) {
			throw new FollowException(INSUFFICIENT_ASSET);
		}
	}

	public void increaseCash(long orderCash) {
		this.cash += CommissionUtil.getCashWithoutCommission(orderCash);
	}
}
