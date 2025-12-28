package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import org.jetbrains.compose.resources.stringResource
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.proposal_show_less
import spaindecides.composeapp.generated.resources.proposal_show_more
import spaindecides.composeapp.generated.resources.proposal_vote_down
import spaindecides.composeapp.generated.resources.proposal_vote_up
import spaindecides.composeapp.generated.resources.report_content

/**
 * Card component for displaying a proposal with voting buttons and expandable description.
 *
 * @param proposal The proposal to display (with user vote information)
 * @param onUpvote Callback when upvote is clicked
 * @param onDownvote Callback when downvote is clicked
 * @param onCardClick Callback when the card is clicked (navigates to detail screen)
 * @param onReportClick Callback when report button is clicked
 * @param modifier Optional modifier
 */
@Composable
fun ProposalCard(
    proposal: ProposalWithUserVote,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onCardClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for expansion (3 lines -> 6 lines)
    var isExpanded by remember { mutableStateOf(false) }

    // State to track if text is truncated (to show/hide expand button)
    var isTextTruncated by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vote buttons column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(40.dp)
            ) {
                // Upvote button
                IconButton(
                    onClick = onUpvote,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (proposal.userVote == 1) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(Res.string.proposal_vote_up),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Vote count
                Text(
                    text = proposal.formattedVotes,
                    style = MaterialTheme.typography.labelLarge,
                    color = when (proposal.userVote) {
                        1 -> MaterialTheme.colorScheme.primary
                        -1 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // Downvote button
                IconButton(
                    onClick = onDownvote,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (proposal.userVote == -1) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.proposal_vote_down),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Proposal content (title + description + expand button)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                // Header row with title and report button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Proposal title
                    Text(
                        text = proposal.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Report button
                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = stringResource(Res.string.report_content),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(top = 4.dp))

                // Proposal description (expandable)
                Text(
                    text = proposal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) 7 else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        // Detect if text is truncated
                        isTextTruncated = textLayoutResult.hasVisualOverflow
                    }
                )

                // Show more/less button (only if text is truncated)
                if (isTextTruncated || isExpanded) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (isExpanded) Res.string.proposal_show_less
                                else Res.string.proposal_show_more
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
